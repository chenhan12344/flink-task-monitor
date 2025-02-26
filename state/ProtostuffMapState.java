package com.demo.state;

import com.demo.utils.ProtostuffRuntimeUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.util.Preconditions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * 基于PB序列化的ValueState
 * 对MapState进行封装，以无感的方式使用带PB序列化的MapState
 * 避免在业务代码中出现大量正反序列化的逻辑
 *
 * @author sky
 * @date 2023-10-27 15:01
 */
public class ProtostuffMapState<UK, UV> implements MapState<UK, UV> {

    /**
     * 被代理的MapState，存储PB序列化后的数据
     */
    private final MapState<UK, byte[]> delegatedState;
    /**
     * 反序列化后的对象类型
     */
    private final Class<UV> valueClass;

    public ProtostuffMapState(MapState<UK, byte[]> delegatedState, Class<UV> valueClass) {
        this.delegatedState = delegatedState;
        this.valueClass = valueClass;
    }

    @Override
    public UV get(UK key) throws Exception {
        byte[] bytes = delegatedState.get(key);
        return ProtostuffRuntimeUtil.deserialize(bytes, valueClass);
    }

    @Override
    public void put(UK key, UV value) throws Exception {
        byte[] serializedBytes = ProtostuffRuntimeUtil.serialize(value);
        delegatedState.put(key, serializedBytes);
    }

    @Override
    public void putAll(Map<UK, UV> map) throws Exception {
        if (MapUtils.isEmpty(map)) {
            return;
        }

        Map<UK, byte[]> serializedMap = new HashMap<>(map.size());

        for (Map.Entry<UK, UV> entry : map.entrySet()) {
            serializedMap.put(entry.getKey(), ProtostuffRuntimeUtil.serialize(entry.getValue()));
        }

        // RocksDBMapState的putAll方法属于攒批写入，所以这里统一转换好后再调用代理状态的putAll尽可能减少I/O
        delegatedState.putAll(serializedMap);
    }

    @Override
    public void remove(UK key) throws Exception {
        delegatedState.remove(key);
    }

    @Override
    public boolean contains(UK key) throws Exception {
        return delegatedState.contains(key);
    }

    @Override
    public Iterable<Map.Entry<UK, UV>> entries() {
        return this::iterator;
    }

    @Override
    public Iterable<UK> keys() throws Exception {
        return () -> new ProtustuffIterator<UK>() {
            @Override
            public UK next() {
                return stateIterator.next().getKey();
            }
        };
    }

    @Override
    public Iterable<UV> values() throws Exception {
        return () -> new ProtustuffIterator<UV>() {
            @Override
            public UV next() {
                Map.Entry<UK, byte[]> entry = stateIterator.next();
                return ProtostuffRuntimeUtil.deserialize(entry.getValue(), valueClass);
            }
        };
    }

    public UV getOrDefault(UK key, UV defaultValue) throws Exception {
        UV value = get(key);
        return value != null ? value : defaultValue;
    }

    public UV computeIfAbsent(UK key, Function<? super UK, ? extends UV> mappingFunction) throws Exception {
        Preconditions.checkNotNull(mappingFunction);
        UV value = get(key);
        if (value != null) {
            return value;
        }

        UV newValue = mappingFunction.apply(key);
        if (newValue != null) {
            put(key, newValue);
        }

        return newValue;
    }

    @Override
    public Iterator<Map.Entry<UK, UV>> iterator() {
        return new ProtostuffMapIterator();
    }

    @Override
    public boolean isEmpty() throws Exception {
        return delegatedState.isEmpty();
    }

    @Override
    public void clear() {
        delegatedState.clear();
    }

    /**
     * PB迭代器，主要用于实现MapState的keys方法和values方法
     */
    private abstract class ProtustuffIterator<T> implements Iterator<T> {

        protected final Iterator<Map.Entry<UK, byte[]>> stateIterator;

        public ProtustuffIterator() {
            try {
                stateIterator = delegatedState.iterator();
            } catch (Exception e) {
                throw new RuntimeException("Exception while decorating internal map state iterator", e);
            }
        }

        @Override
        public boolean hasNext() {
            return stateIterator.hasNext();
        }

        @Override
        public void remove() {
            stateIterator.remove();
        }
    }

    /**
     * PbMapState迭代器，用于迭代遍历MapState
     */
    private class ProtostuffMapIterator extends ProtustuffIterator<Map.Entry<UK, UV>> {
        @Override
        public Map.Entry<UK, UV> next() {
            Map.Entry<UK, byte[]> currentEntry = stateIterator.next();
            UK key = currentEntry.getKey();
            UV entryValue = ProtostuffRuntimeUtil.deserialize(currentEntry.getValue(), valueClass);
            return new ProtostuffEntry<>(key, entryValue);
        }
    }

    /**
     * 状态描述符简化创建方法
     *
     * @param name     描述符名称
     * @param keyClass MapState键类型
     * @param <UK>     MapState键类型
     * @return MapState描述符
     */
    public static <UK> MapStateDescriptor<UK, byte[]> descriptor(String name, Class<UK> keyClass) {
        return new MapStateDescriptor<>(name, keyClass, byte[].class);
    }
}
