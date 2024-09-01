package com.github.cao.awa.sepals.weight;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.weight.result.WeightingResult;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.random.Random;

import java.util.*;
import java.util.function.ToIntFunction;

public class WeightTable<T> {
    private Ranged<T>[] weighted;
    private int range;

    @SuppressWarnings("unchecked")
    public static <X extends Weighted> WeightTable<X> initWeight(Catheter<X> pool) {
        int size = pool.count();
        Ranged<X>[] ranges = new Range[size];
        int range = 0;
        int i = 0;
        while (i < size) {
            X weighted = pool.fetch(i);
            int nextRange = range + weighted.getWeight().getValue();
            ranges[i] = new Range<>(range, nextRange, weighted);
            range = nextRange;
            i++;
        }
        return new WeightTable<X>().initWeightWithPrecalculate(ranges, Math.max(range, 1));
    }

    @SuppressWarnings("unchecked")
    public static <X extends Weighted> WeightTable<X> initWeight(List<X> pool) {
        X[] elements = (X[]) new Weighted[pool.size()];
        pool.toArray(elements);
        return initWeight(elements, weighted -> weighted.getWeight().getValue());
    }

    @SuppressWarnings("unchecked")
    public static <X> WeightTable<X> initWeight(Collection<X> pool, ToIntFunction<X> weightGenerator) {
        int size = pool.size();
        X[] elements = (X[]) new Object[size];
        pool.toArray(elements);
        Ranged<X>[] ranges = new Range[size];
        int range = 0;
        int i = 0;
        while (i < size) {
            X weighted = elements[i];
            int nextRange = range + weightGenerator.applyAsInt(weighted);
            ranges[i] = new Range<>(range, nextRange, weighted);
            range = nextRange;
            i++;
        }
        return new WeightTable<X>().initWeightWithPrecalculate(ranges, Math.max(range, 1));
    }

    @SuppressWarnings("unchecked")
    public static <X> WeightTable<X> initWeight(X[] pool, ToIntFunction<X> weightGenerator) {
        int size = pool.length;
        Ranged<X>[] ranges = new Range[size];
        int range = 0;
        int i = 0;
        while (i < size) {
            X weighted = pool[i];
            int nextRange = range + weightGenerator.applyAsInt(weighted);
            ranges[i] = new Range<>(range, nextRange, weighted);
            range = nextRange;
            i++;
        }
        return new WeightTable<X>().initWeightWithPrecalculate(ranges, Math.max(range, 1));
    }

    @SuppressWarnings("unchecked")
    public static <X> WeightTable<X> initWeight(Pair<X, Integer>[] pool) {
        int size = pool.length;
        Ranged<X>[] ranges = new Range[size];
        int range = 0;
        int i = 0;
        while (i < size) {
            Pair<X, Integer> pair = pool[i];
            X x = pair.getFirst();
            int nextRange = range + pair.getSecond();
            ranges[i] = new Range<>(range, nextRange, x);
            range = nextRange;
            i++;
        }
        return new WeightTable<X>().initWeightWithPrecalculate(ranges, Math.max(range, 1));
    }

    public WeightTable<T> initWeightWithPrecalculate(Ranged<T>[] weighted, int range) {
        this.weighted = weighted;
        this.range = range;
        return this;
    }

    public T select(Random random) {
        WeightingResult<T> result = selectRange(random);
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
    public WeightingResult<T> selectRange(Random random) {
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

    public WeightingResult<T> selectWithIndex(int index) {
        if (index < this.weighted.length) {
            return new WeightingResult<>(this.weighted[index].element(), index);
        }
        return new WeightingResult<>(null, index);
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
