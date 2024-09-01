package com.github.cao.awa.sepals.weight;

import com.github.cao.awa.catheter.Catheter;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.math.random.Random;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class WeightedList<U> {
    protected final Catheter<WeightedList.Entry<U>> entries;
    protected Catheter<U> elements;

    public WeightedList() {
        this.entries = Catheter.make();
        this.elements = Catheter.make();
    }

    public WeightedList<U> add(U data, int weight) {
        this.entries.append(new WeightedList.Entry<>(data, weight));
        varyElements();
        return this;
    }

    public WeightedList<U> shuffle() {
        this.entries.sort(Comparator.comparingDouble(WeightedList.Entry::getShuffledOrder));
        this.elements = this.entries.vary((WeightedList.Entry<U> entry) -> entry.getElement());
        return this;
    }

    private void varyElements() {
        this.elements = this.entries.vary((WeightedList.Entry<U> entry) -> entry.getElement());
    }

    public Catheter<U> elements() {
        return this.elements.dump();
    }

    public String toString() {
        return "ShufflingList[" + this.entries + "]";
    }

    public static class Entry<T> {
        private static final Random random = Random.create();
        final T data;
        final int weight;

        Entry(T data, int weight) {
            this.weight = weight;
            this.data = data;
        }

        private double getShuffledOrder() {
            return -Math.pow(random.nextFloat(), 1.0F / (float)this.weight);
        }

        public T getElement() {
            return this.data;
        }

        public int getWeight() {
            return this.weight;
        }

        public String toString() {
            return this.weight + ":" + this.data;
        }
    }
}
