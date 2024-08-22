package com.github.cao.awa.sepals.collection.binary.set;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Arrays;
import java.util.stream.Stream;

public class ReadonlyBinaryIntegerList implements BinarySearchList<Integer> {
    private final int[] elements;

    public ReadonlyBinaryIntegerList(int[] elements) {
        this.elements = elements;
    }

    public ReadonlyBinaryIntegerList(Integer[] elements) {
        this.elements = new int[elements.length];
        for (int i = 0, elementsLength = elements.length; i < elementsLength; i++) {
            this.elements[i] = elements[i];
        }
    }

    public boolean contains(int expected) {
        int[] elements = this.elements;
        int size = elements.length;
        if (size == 0) {
            return false;
        }

        int maxEdge = size - 1;
        int index = maxEdge / 2;

        int dynamicMaxEdge = maxEdge;

        // Binary search.
        while (index < size && index > -1) {
            int current = elements[index];

            if (current == expected) {
                return true;
            }

            if (current < expected) {
                // Update index(bottom edge).
                index += Math.max((dynamicMaxEdge - index) / 2, 1);
            } else {
                // Update index and adjust the up edge.
                dynamicMaxEdge = index;
                index -= Math.max(index / 2, 1);
            }

            if (index == dynamicMaxEdge) {
                break;
            }
        }

        return false;
    }

    @Override
    public boolean containsElement(Integer element) {
        return contains(element);
    }

    @Override
    public Integer get(int index) {
        return this.elements[index];
    }

    @Override
    public int size() {
        return this.elements.length;
    }

    @Override
    public Stream<Integer> stream() {
        return Arrays.stream(this.elements).boxed();
    }

    @Override
    public Iterable<Integer> iterable() {
        return IntArrayList.of(this.elements);
    }
}
