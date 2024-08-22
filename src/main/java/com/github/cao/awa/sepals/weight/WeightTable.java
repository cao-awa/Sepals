package com.github.cao.awa.sepals.weight;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.weight.result.WeightingResult;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class WeightTable<T extends Weighted> {
    private Ranged<T>[] weighted;
    private int range;

    @SuppressWarnings("unchecked")
    public WeightTable<T> initWeight(Catheter<T> pool) {
        int size = pool.count();
        Range<T>[] ranges = new Range[size];
        int range = 0;
        int i = 0;
        while (i < size) {
            T weighted = pool.fetch(i);
            int nextRange = range + weighted.getWeight().getValue();
            ranges[i] = new Range<>(range, nextRange, weighted);
            range = nextRange;
            i++;
        }
        this.weighted = ranges;
        this.range = Math.max(range, 1);
        return this;
    }

    @SuppressWarnings("unchecked")
    public WeightTable<T> initWeight(List<T> pool) {
        int size = pool.size();
        T[] elements = (T[]) new Weighted[size];
        pool.toArray(elements);
        Range<T>[] ranges = new Range[size];
        int range = 0;
        int i = 0;
        while (i < size) {
            T weighted = elements[i];
            int nextRange = range + weighted.getWeight().getValue();
            ranges[i] = new Range<>(range, nextRange, weighted);
            range = nextRange;
            i++;
        }
        this.weighted = ranges;
        this.range = Math.max(range, 1);
        return this;
    }

    public WeightTable<T> initWeightWithPrecalculate(Ranged<T>[] weighted, int range) {
        this.weighted = weighted;
        this.range = range;
        return this;
    }

    public T select(Random random) {
        WeightingResult<T> result = selectWithIndex(random);
        if (result == null) {
            return null;
        }
        return result.value();
    }

    /**
     * Select a weighted element by binary search.
     * <p>
     * When all weight all be zero(range==0) then direct return a random element.
     *
     * @param random a minecraft random generator
     * @author cao_awa
     * @since 1.0.0
     */
    public WeightingResult<T> selectWithIndex(Random random) {
        // Push in stack to improves performance.
        Ranged<T>[] ranges = this.weighted;

        if (ranges.length == 1) {
            return new WeightingResult<>(ranges[0].element(), 0);
        }

        // When range is one then means all weighted element are all weighted zero.
        // Do not care the weights, direct random to select.
        if (this.range == 1) {
            int index = random.nextInt(ranges.length);
            return new WeightingResult<>(ranges[index].element(), index);
        }

        int expected = random.nextInt(this.range);

        int size = ranges.length;
        int maxEdge = size - 1;
        int index = maxEdge / 2;

        Range<Object> weightedEdge = new Range<>(0, maxEdge, null);

        int dynamicMaxEdge = maxEdge;

        // Binary search.
        while (index < size && index > -1) {
            // Should check the edge, return null when index out of bounds.
            if (weightedEdge.isIn(index)) {
                Ranged<T> range = ranges[index];

                if (range.isIn(expected)) {
                    return new WeightingResult<>(range.element(), index);
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

    public static final class Range<T> implements Ranged<T> {
        private final int min;
        private final int max;
        private final T element;

        public Range(int min, int max, T element) {
            this.min = min;
            this.max = max;
            this.element = element;
        }

        public int min() {
            return this.min;
        }

        public int max() {
            return this.max;
        }

        public T element() {
            return this.element;
        }
    }

    public interface Ranged<T> {
        default boolean isIn(int value) {
            return !(min() > value || max() < value);
        }

        default boolean isSmaller(int value) {
            return min() > value;
        }

        default boolean isBigger(int value) {
            return max() < value;
        }

        int min();

        int max();

        T element();
    }
}
