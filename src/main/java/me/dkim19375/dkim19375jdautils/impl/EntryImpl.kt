package me.dkim19375.dkim19375jdautils.impl;

import java.util.Map;

public class EntryImpl<K, V> implements Map.Entry<K, V> {
    private final K key;
    private V value;

    public EntryImpl(final K key, final V value) {
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
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }
}