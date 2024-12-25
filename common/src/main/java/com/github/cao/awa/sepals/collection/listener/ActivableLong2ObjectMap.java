package com.github.cao.awa.sepals.collection.listener;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("unchecked")
public class ActivableLong2ObjectMap<V> implements Long2ObjectMap<V> {
    private final ActiveTrigger<V>[] triggers = new ActiveTrigger[ActivableLong2ObjectMap.ActiveTriggerType.values().length];
    private final Long2ObjectMap<V> delegate;

    public ActivableLong2ObjectMap(Long2ObjectMap<V> delegate) {
        this.delegate = delegate;
    }

    public ActivableLong2ObjectMap<V> triggerPut(MapPutTrigger<V> putTrigger) {
        this.triggers[ActiveTriggerType.PUT.ordinal()] = putTrigger;
        return this;
    }

    public ActivableLong2ObjectMap<V> triggerRemove(MapRemoveTrigger<V> removeTrigger) {
        this.triggers[ActiveTriggerType.REMOVE.ordinal()] = removeTrigger;
        return this;
    }

    public ActivableLong2ObjectMap<V> triggerPutAndRemoved(MapRemovedTrigger<V> removeTrigger) {
        this.triggers[ActiveTriggerType.REMOVED.ordinal()] = removeTrigger;
        return this;
    }

    public ActivableLong2ObjectMap<V> triggerPutAndRemove(MapPutTrigger<V> putTrigger, MapRemoveTrigger<V> removeTrigger) {
        this.triggers[ActiveTriggerType.PUT.ordinal()] = putTrigger;
        this.triggers[ActiveTriggerType.REMOVE.ordinal()] = removeTrigger;
        return this;
    }

    public ActivableLong2ObjectMap<V> triggerPutAndRemoved(MapPutTrigger<V> putTrigger, MapRemovedTrigger<V> removeTrigger) {
        this.triggers[ActiveTriggerType.PUT.ordinal()] = putTrigger;
        this.triggers[ActiveTriggerType.REMOVED.ordinal()] = removeTrigger;
        return this;
    }

    public ActivableLong2ObjectMap<V> triggerClear(MapClearTrigger<V> clearTrigger) {
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
    public V put(long key, V value) {
        ActiveTrigger<V> trigger = trigger(ActiveTriggerType.PUT);
        if (trigger != null) {
            trigger.trigger(key, value, this);
        }
        return this.delegate.put(key, value);
    }

    @Override
    public V get(long key) {
        return this.delegate.get(key);
    }

    @Override
    public V remove(long key) {
        V previousValue = this.delegate.remove(key);
        ActiveTrigger<V> removeTrigger = trigger(ActiveTriggerType.REMOVE);
        if (removeTrigger != null) {
            removeTrigger.trigger(key, null, this);
        }
        ActiveTrigger<V> removedTrigger = trigger(ActiveTriggerType.REMOVED);
        if (removedTrigger != null) {
            removedTrigger.trigger(key, previousValue, this);
        }
        return previousValue;
    }

    @Override
    public void putAll(@NotNull Map<? extends Long, ? extends V> m) {
        ActiveTrigger<V> trigger = trigger(ActiveTriggerType.PUT);
        if (trigger != null) {
            m.forEach((k, v) -> trigger.trigger(k, v, this));
        }
        this.delegate.putAll(m);
    }

    @Override
    public void clear() {
        ActiveTrigger<V> trigger = trigger(ActiveTriggerType.CLEAR);

        if (trigger != null) {
            trigger.trigger(-1, null, this);
        }
        this.delegate.clear();
    }

    @Override
    public void defaultReturnValue(V rv) {
        this.delegate.defaultReturnValue(rv);
    }

    @Override
    public V defaultReturnValue() {
        return this.delegate.defaultReturnValue();
    }

    @Override
    public ObjectSet<Entry<V>> long2ObjectEntrySet() {
        return this.delegate.long2ObjectEntrySet();
    }

    @Override
    @NotNull
    public LongSet keySet() {
        return this.delegate.keySet();
    }

    @Override
    public ObjectCollection<V> values() {
        return this.delegate.values();
    }

    @Override
    public boolean containsKey(long key) {
        return this.delegate.containsKey(key);
    }

    @Override
    public ObjectSet<Map.Entry<Long, V>> entrySet() {
        return this.delegate.entrySet();
    }

    public ActiveTrigger<V> trigger(ActiveTriggerType type) {
        return this.triggers[type.ordinal()];
    }

    public enum ActiveTriggerType {
        PUT,
        REMOVE,
        REMOVED,
        CLEAR,
    }

    public interface ActiveTrigger<V> {
        void trigger(long k, V v, ActivableLong2ObjectMap<V> map);
    }

    public interface MapPutTrigger<V> extends ActiveTrigger<V> {
        @Override
        default void trigger(long k, V v, ActivableLong2ObjectMap<V> map) {
            trigger(k, v);
        }

        void trigger(long key, V value);
    }

    public interface MapRemoveTrigger<V> extends ActiveTrigger<V> {
        @Override
        default void trigger(long k, V v, ActivableLong2ObjectMap<V> map) {
            trigger(k);
        }

        void trigger(long key);
    }

    public interface MapRemovedTrigger<V> extends ActiveTrigger<V> {
        @Override
        default void trigger(long k, V v, ActivableLong2ObjectMap<V> map) {
            trigger(k, v);
        }

        void trigger(long key, V previousValue);
    }

    public interface MapClearTrigger<V> extends ActiveTrigger<V> {
        @Override
        default void trigger(long k, V v, ActivableLong2ObjectMap<V> map) {
            trigger(map);
        }

        void trigger(ActivableLong2ObjectMap<V> map);
    }
}