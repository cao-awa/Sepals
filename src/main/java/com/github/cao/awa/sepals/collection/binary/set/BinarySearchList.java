package com.github.cao.awa.sepals.collection.binary.set;

import java.util.stream.Stream;

public interface BinarySearchList<T> {
    boolean containsElement(T element);
    T get(int index);
    int size();
    Stream<T> stream();
    Iterable<T> iterable();
}
