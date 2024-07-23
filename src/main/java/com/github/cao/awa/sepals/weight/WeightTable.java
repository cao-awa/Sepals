package com.github.cao.awa.sepals.weight;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
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

    public WeightTable<T> initWeight(List<T> pool) {
        int size = pool.size();
        this.weighted = new Range[size];
        int range = 0;
        for (int i = 0; i < size; i ++) {
            T weighted = pool.get(i);
            Weight weight = weighted.getWeight();
            int weightValue = weight.getValue();
            int nextRange = range + weightValue;
            this.weighted[i] = new Range<>(range, nextRange, weighted);
            range = nextRange;
        }
        this.range = Math.max(range, 1);
        return this;
    }

    public T select(Random random) {
        if (this.weighted.length == 1) {
            return this.weighted[0].element();
        }

        if (this.range == 1) {
            return this.weighted[0].element();
        }

        int expected = random.nextInt(this.range);

        int maxEdge = this.weighted.length - 1;
        int index = maxEdge / 2;

        Range<Object> weightedEdge = new Range<>(0, maxEdge, null);

        int dynamicMaxEdge = maxEdge;

        while (true) {
            if (weightedEdge.isIn(index)) {
                Range<T> range = this.weighted[index];

                if (range.isIn(expected)) {
                    return range.element();
                }

                if (range.isBigger(expected)) {
                    index += Math.max((dynamicMaxEdge - index) / 2, 1);
                } else {
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
