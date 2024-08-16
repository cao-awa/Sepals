package com.github.cao.awa.sepals.weight;

import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class WeightTable<T extends Weighted> {
    private Range<T>[] weighted;
    private int range;

    @SuppressWarnings("unchecked")
    public WeightTable<T> initWeight(List<T> pool) {
        T[] elements = (T[]) pool.toArray(Weighted[]::new);
        int size = elements.length;
        Range<T>[] ranges = new Range[size];
        int range = 0;
        for (int i = 0; i < size; i ++) {
            T weighted = elements[i];
            int nextRange = range + weighted.getWeight().getValue();
            ranges[i] = new Range<>(range, nextRange, weighted);
            range = nextRange;
        }
        this.weighted = ranges;
        this.range = Math.max(range, 1);
        return this;
    }

    /**
     * Select a weighted element by binary search.
     * <p>
     * When all weight all be zero(range==0) then direct return a random element.
     *
     * @param random a minecraft random generator
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    public T select(Random random) {
        // Push in stack to improves performance.
        Range<T>[] ranges = this.weighted;

        // When range is one then means all weighted element are all weighted zero.
        // Do not care the weights, direct random to select.
        if (this.range == 1) {
            return ranges[random.nextInt(ranges.length)].element();
        }

        int expected = random.nextInt(this.range);

        int maxEdge = ranges.length - 1;
        int index = maxEdge / 2;

        Range<Object> weightedEdge = new Range<>(0, maxEdge, null);

        int dynamicMaxEdge = maxEdge;

        // Binary search.
        while (true) {
            // Should check the edge, return null when index out of bounds.
            if (weightedEdge.isIn(index)) {
                Range<T> range = ranges[index];

                if (range.isIn(expected)) {
                    return range.element();
                }

                if (range.isBigger(expected)) {
                    // Update index(bottom edge).
                    index += Math.max((dynamicMaxEdge - index) / 2, 1);
                } else {
                    // Update index and adjust the up edge.
                    dynamicMaxEdge = index;
                    index -= Math.max(index / 2, 1);
                }
            } else {
                break;
            }
        }

        return null;
    }

    private record Range<T>(int min, int max, T element) {
        public boolean isIn(int value) {
            return !(min() > value || max() < value);
        }

        public boolean isSmaller(int value) {
            return min() > value;
        }

        public boolean isBigger(int value) {
            return max() < value;
        }
    }
}
