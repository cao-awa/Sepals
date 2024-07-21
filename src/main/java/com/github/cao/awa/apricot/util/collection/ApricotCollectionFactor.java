package com.github.cao.awa.apricot.util.collection;

import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApricotCollectionFactor {
    public static <V> V[] array(int size) {
        return Manipulate.cast(new Object[size]);
    }

    public static <K, V> HashBiMap<K, V> hashBiMap() {
        return HashBiMap.create();
    }

    public static <K, V> HashBiMap<K, V> hashBiMap(Map<K, V> map) {
        return HashBiMap.create(map);
    }

    public static <K, V> Object2ObjectOpenHashMap<K, V> hashMap() {
                return new Object2ObjectOpenHashMap<>();
    }

    public static <K, V> Object2ObjectOpenHashMap<K, V> hashMap(Map<K, V> map) {
        return new Object2ObjectOpenHashMap<>(map);
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap() {
        return new LinkedHashMap<>();
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(Map<K, V> map) {
        return new LinkedHashMap<>(map);
    }

    public static <K, V> ConcurrentHashMap<K, V> concurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    public static <V> List<V> syncList() {
        return Collections.synchronizedList(arrayList());
    }

    public static <V> List<V> syncList(int capacity) {
        return Collections.synchronizedList(arrayList(capacity));
    }

    public static <V> List<V> syncList(Collection<V> delegate) {
        return Collections.synchronizedList(arrayList(delegate));
    }

    public static <V> List<V> syncList(V[] delegate) {
        return Collections.synchronizedList(arrayList(delegate));
    }

    public static <V> ObjectArrayList<V> arrayList() {
        return new ObjectArrayList<>();
    }

    public static <V> ObjectArrayList<V> arrayList(int capacity) {
        return new ObjectArrayList<>(capacity);
    }

    public static <V> ObjectArrayList<V> arrayList(Collection<V> delegate) {
        return new ObjectArrayList<>(delegate);
    }

    public static <V> ObjectArrayList<V> arrayList(V[] delegate) {
        return new ObjectArrayList<>(delegate);
    }

    public static <V> LinkedList<V> linkedList() {
        return new LinkedList<>();
    }

    public static <V> ObjectOpenHashSet<V> hashSet() {
        return new ObjectOpenHashSet<>();
    }

    public static <V> ObjectOpenHashSet<V> hashSet(int capacity) {
        return new ObjectOpenHashSet<>(capacity);
    }

    public static <V> ObjectOpenHashSet<V> hashSet(Collection<V> delegate) {
        return new ObjectOpenHashSet<>(delegate);
    }

    public static <V> ObjectOpenHashSet<V> hashSet(V[] delegate) {
        return new ObjectOpenHashSet<>(delegate);
    }
    public static <V> Set<V> concurrentHashSet() {
        return Sets.newConcurrentHashSet();
    }

    public static <V> Stack<V> stack() {
        return new Stack<>();
    }
}
