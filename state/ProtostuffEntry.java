package com.demo.state;

import java.util.Map;

/**
 * PB反序列化后的MapEntry，用于迭代遍历内部MapState时将迭代的Entry结果返回给用户
 *
 * @author sky
 */
class ProtostuffEntry<K, V> implements Map.Entry<K, V> {

    private final K key;
    private V value;

    public ProtostuffEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        return this.value = value;
    }
}
