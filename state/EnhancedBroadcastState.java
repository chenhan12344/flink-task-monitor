package com.demo.state;

import org.apache.flink.api.common.state.BroadcastState;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * @author sky
 */
public class EnhancedBroadcastState<K, V> implements BroadcastState<K, V> {

    private final BroadcastState<K, V> delegatedState;

    public EnhancedBroadcastState(BroadcastState<K, V> delegatedState) {
        this.delegatedState = delegatedState;
    }

    @Override
    public void put(K key, V value) throws Exception {
        delegatedState.put(key, value);
    }

    @Override
    public void putAll(Map<K, V> map) throws Exception {
        delegatedState.putAll(map);
    }

    @Override
    public void remove(K key) throws Exception {
        delegatedState.remove(key);
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() throws Exception {
        return delegatedState.iterator();
    }

    @Override
    public Iterable<Map.Entry<K, V>> entries() throws Exception {
        return delegatedState.entries();
    }

    public V getOrDefault(K key, V defaultValue) throws Exception {
        V value = delegatedState.get(key);
        if (value != null) {
            return value;
        }

        return defaultValue;
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) throws Exception {
        V value = delegatedState.get(key);
        if (value != null) {
            return value;
        }

        value = mappingFunction.apply(key);
        if (value != null) {
            delegatedState.put(key, value);
        }

        return value;
    }

    public V get(K key) throws Exception {
        return delegatedState.get(key);
    }

    @Override
    public boolean contains(K key) throws Exception {
        return delegatedState.contains(key);
    }

    @Override
    public Iterable<Map.Entry<K, V>> immutableEntries() throws Exception {
        return delegatedState.immutableEntries();
    }

    @Override
    public void clear() {
        delegatedState.clear();
    }
}
