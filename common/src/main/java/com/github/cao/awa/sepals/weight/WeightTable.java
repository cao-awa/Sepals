package com.github.cao.awa.sepals.weight;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.entity.ai.task.SepalsLongJumpTask;
import com.github.cao.awa.sepals.weight.result.WeightingResult;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.random.Random;

import java.util.*;
import java.util.function.ToIntFunction;

public interface WeightTable<T> extends Ranged<T> {

}

interface Ranged<T> {
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