package com.demo.state;

import com.demo.utils.ProtostuffRuntimeUtil;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;

import java.util.Iterator;
import java.util.Map;

/**
 * 基于PB序列化的ReadOnlyBroadcastState
 * 对ReadOnlyBroadcastState进行封装，以无感的方式使用带PB序列化的ReadOnlyBroadcastState
 * 避免在业务代码中出现大量正反序列化的逻辑
 *
 * @author sky
 */
public class ReadOnlyProtostuffBroadcastState<K, V> implements ReadOnlyBroadcastState<K, V> {

    private final ReadOnlyBroadcastState<K, byte[]> delegatedReadOnlyState;
    protected final Class<V> valueClass;

    public ReadOnlyProtostuffBroadcastState(ReadOnlyBroadcastState<K, byte[]> delegatedReadOnlyState, Class<V> valueClass) {
        this.delegatedReadOnlyState = delegatedReadOnlyState;
        this.valueClass = valueClass;
    }

    @Override
    public V get(K key) throws Exception {
        return ProtostuffRuntimeUtil.deserialize(delegatedReadOnlyState.get(key), valueClass);
    }

    @Override
    public boolean contains(K key) throws Exception {
        return delegatedReadOnlyState.contains(key);
    }

    @Override
    public Iterable<Map.Entry<K, V>> immutableEntries() throws Exception {
        return new EntrySetWrapper(delegatedReadOnlyState.immutableEntries());
    }

    @Override
    public void clear() {
        delegatedReadOnlyState.clear();
    }

    protected class IteratorWrapper implements Iterator<Map.Entry<K, V>> {

        private final Iterator<Map.Entry<K, byte[]>> delegatedIterator;

        IteratorWrapper(Iterator<Map.Entry<K, byte[]>> delegatedIterator) {
            this.delegatedIterator = delegatedIterator;
        }

        @Override
        public boolean hasNext() {
            return delegatedIterator.hasNext();
        }

        @Override
        public Map.Entry<K, V> next() {
            Map.Entry<K, byte[]> entry = delegatedIterator.next();
            K key = entry.getKey();
            V entryValue = ProtostuffRuntimeUtil.deserialize(entry.getValue(), valueClass);
            return new ProtostuffEntry<>(key, entryValue);
        }
    }

    protected class EntrySetWrapper implements Iterable<Map.Entry<K, V>> {

        private final Iterable<Map.Entry<K, byte[]>> delegatedEntrySet;

        EntrySetWrapper(Iterable<Map.Entry<K, byte[]>> delegatedEntrySet) {
            this.delegatedEntrySet = delegatedEntrySet;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new IteratorWrapper(delegatedEntrySet.iterator());
        }
    }

}
