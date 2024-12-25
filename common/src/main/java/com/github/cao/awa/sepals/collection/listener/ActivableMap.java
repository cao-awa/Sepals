package com.github.cao.awa.sepals.collection.listener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class ActivableMap<K, V> implements Map<K, V> {
    private final ActiveTrigger<K, V>[] triggers = new ActiveTrigger[ActiveTriggerType.values().length];
    private final Map<K, V> delegate;

    public ActivableMap(Map<K, V> delegate) {
        this.delegate = delegate;
    }

    public ActivableMap<K, V> triggerPut(MapPutTrigger<K, V> putTrigger) {
        this.triggers[ActiveTriggerType.PUT.ordinal()] = putTrigger;
        return this;
    }

    public ActivableMap<K, V> triggerRemove(MapRemoveTrigger<K, V> removeTrigger) {
        this.triggers[ActiveTriggerType.REMOVE.ordinal()] = removeTrigger;
        return this;
    }

    public ActivableMap<K, V> triggerPutAndRemoved(MapRemovedTrigger<K, V> removeTrigger) {
        this.triggers[ActiveTriggerType.REMOVED.ordinal()] = removeTrigger;
        return this;
    }

    public ActivableMap<K, V> triggerPutAndRemove(MapPutTrigger<K, V> putTrigger, MapRemoveTrigger<K, V> removeTrigger) {
        this.triggers[ActiveTriggerType.PUT.ordinal()] = putTrigger;
        this.triggers[ActiveTriggerType.REMOVE.ordinal()] = removeTrigger;
        return this;
    }

    public ActivableMap<K, V> triggerPutAndRemoved(MapPutTrigger<K, V> putTrigger, MapRemovedTrigger<K, V> removeTrigger) {
        this.triggers[ActiveTriggerType.PUT.ordinal()] = putTrigger;
        this.triggers[ActiveTriggerType.REMOVED.ordinal()] = removeTrigger;
        return this;
    }

    public ActivableMap<K, V> triggerClear(MapClearTrigger<K, V> clearTrigger) {
        this.triggers[ActiveTriggerType.CLEAR.ordinal()] = clearTrigger;
        return this;
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.delegate.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        trigger(ActiveTriggerType.REMOVE).trigger(key, value, this);
        return this.delegate.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V previousValue = this.delegate.remove(key);
        trigger(ActiveTriggerType.REMOVE).trigger((K) key, null, this);
        trigger(ActiveTriggerType.REMOVED).trigger((K) key, previousValue, this);
        return previousValue;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        ActiveTrigger<K, V> trigger = trigger(ActiveTriggerType.PUT);
        m.forEach((k, v) -> trigger.trigger(k, v, this));
        this.delegate.putAll(m);
    }

    @Override
    public void clear() {
        trigger(ActiveTriggerType.CLEAR).trigger(null, null, this);
        this.delegate.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return this.delegate.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return this.delegate.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.delegate.entrySet();
    }

    public ActiveTrigger<K, V> trigger(ActiveTriggerType type) {
        return this.triggers[type.ordinal()];
    }

    public enum ActiveTriggerType {
        PUT,
        REMOVE,
        REMOVED,
        CLEAR,
    }

    public interface ActiveTrigger<K, V> {
        void trigger(K k, V v, ActivableMap<K, V> map);
    }

    public interface MapPutTrigger<K, V> extends ActiveTrigger<K, V> {
        @Override
        default void trigger(K k, V v, ActivableMap<K, V> map) {
            trigger(k, v);
        }

        void trigger(K key, V value);
    }

    public interface MapRemoveTrigger<K, V> extends ActiveTrigger<K, V> {
        @Override
        default void trigger(K k, V v, ActivableMap<K, V> map) {
            trigger(k);
        }

        void trigger(K key);
    }

    public interface MapRemovedTrigger<K, V> extends ActiveTrigger<K, V> {
        @Override
        default void trigger(K k, V v, ActivableMap<K, V> map) {
            trigger(k, v);
        }

        void trigger(K key, V previousValue);
    }

    public interface MapClearTrigger<K, V> extends ActiveTrigger<K, V> {
        @Override
        default void trigger(K k, V v, ActivableMap<K, V> map) {
            trigger(map);
        }

        void trigger(Map<K, V> map);
    }
}
