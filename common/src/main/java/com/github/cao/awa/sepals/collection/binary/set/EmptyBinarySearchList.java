package com.github.cao.awa.sepals.collection.binary.set;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;

import java.util.stream.Stream;

public class EmptyBinarySearchList<T> implements BinarySearchList<T> {
    @Override
    public boolean containsElement(T element) {
        return false;
    }

    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Stream<T> stream() {
        return Stream.empty();
    }

    @Override
    public Iterable<T> iterable() {
        return ApricotCollectionFactor.arrayList();
    }
}
