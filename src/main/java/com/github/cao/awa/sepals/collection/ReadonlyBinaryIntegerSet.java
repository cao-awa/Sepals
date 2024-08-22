package com.github.cao.awa.sepals.collection;

import com.github.cao.awa.sepals.weight.WeightTable;

import java.util.Random;

public class ReadonlyBinaryIntegerSet {
    private final int[] elements;

    public ReadonlyBinaryIntegerSet(int[] elements) {
        this.elements = elements;
    }

    public ReadonlyBinaryIntegerSet(Integer[] elements) {
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

    public static void main(String[] args) {
        Random random = new Random();
        int[] elements = random.ints(16384000).toArray();

        ReadonlyBinaryIntegerSet set = new ReadonlyBinaryIntegerSet(elements);

        System.out.println(set.contains(201));

        long start = System.currentTimeMillis();

        set.contains(201);

        System.out.println("Done in: " + (System.currentTimeMillis() - start) + "ms");
    }
}
