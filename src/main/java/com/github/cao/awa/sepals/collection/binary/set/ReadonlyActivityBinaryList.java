package com.github.cao.awa.sepals.collection.binary.set;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.Activity;

import java.util.Comparator;
import java.util.stream.Stream;

public class ReadonlyActivityBinaryList<T extends Activity> implements BinarySearchList<T> {
    private final T[] elements;

    public ReadonlyActivityBinaryList(T[] elements) {
        this.elements = elements;
        ObjectArrays.quickSort(this.elements, Comparator.comparingInt(Activity::hashCode));
    }

    public boolean contains(T expected) {
        T[] elements = this.elements;

        int expectedId = expected.hashCode();

        int size = elements.length;
        if (size == 0) {
            return false;
        }

        int maxEdge = size - 1;
        int index = maxEdge / 2;

        int dynamicMaxEdge = maxEdge;

        // Binary search.
        while (index < size && index > -1) {
            int current = elements[index].hashCode();

            if (current == expectedId) {
                return true;
            }

            if (current < expectedId) {
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
    public boolean containsElement(T element) {
        return contains(element);
    }

    @Override
    public T get(int index) {
        return this.elements[index];
    }

    @Override
    public int size() {
        return this.elements.length;
    }

    @Override
    public Stream<T> stream() {
        return Stream.of(this.elements);
    }

    @Override
    public Iterable<T> iterable() {
        return ApricotCollectionFactor.arrayList(this.elements);
    }
}
