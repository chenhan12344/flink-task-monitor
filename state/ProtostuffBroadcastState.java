package com.demo.state;

import com.demo.utils.ProtostuffRuntimeUtil;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.util.Preconditions;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * 基于PB序列化的BroadcastState
 * 对BroadcastState进行封装，以无感的方式使用带PB序列化的BroadcastState
 * 避免在业务代码中出现大量正反序列化的逻辑
 *
 * @author sky
 */
public class ProtostuffBroadcastState<K, V> extends ReadOnlyProtostuffBroadcastState<K, V> implements BroadcastState<K, V> {

    private final BroadcastState<K, byte[]> delegatedState;

    public ProtostuffBroadcastState(BroadcastState<K, byte[]> delegatedState, Class<V> valueClass) {
        super(delegatedState, valueClass);
        this.delegatedState = delegatedState;
    }

    @Override
    public void put(K key, V value) throws Exception {
        delegatedState.put(key, ProtostuffRuntimeUtil.serialize(value));
    }

    @Override
    public void putAll(Map<K, V> map) throws Exception {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            delegatedState.put(entry.getKey(), ProtostuffRuntimeUtil.serialize(entry.getValue()));
        }
    }

    @Override
    public void remove(K key) throws Exception {
        delegatedState.remove(key);
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() throws Exception {
        return new IteratorWrapper(delegatedState.iterator());
    }

    @Override
    public Iterable<Map.Entry<K, V>> entries() throws Exception {
        return new EntrySetWrapper(delegatedState.entries());
    }

    @Override
    public V get(K key) throws Exception {
        return ProtostuffRuntimeUtil.deserialize(delegatedState.get(key), valueClass);
    }

    public V getOrDefault(K key, V defaultValue) throws Exception {
        V value = get(key);
        return value != null ? value : defaultValue;
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) throws Exception {
        Preconditions.checkNotNull(mappingFunction);
        V value = get(key);
        if (value != null) {
            return value;
        }

        V newValue = mappingFunction.apply(key);
        if (newValue != null) {
            put(key, newValue);
        }

        return newValue;
    }

}
