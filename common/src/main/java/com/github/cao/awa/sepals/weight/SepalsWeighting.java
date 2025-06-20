package com.github.cao.awa.sepals.weight;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.catheter.receptacle.IntegerReceptacle;
import com.github.cao.awa.catheter.receptacle.LongReceptacle;
import com.github.cao.awa.catheter.receptacle.Receptacle;
import com.github.cao.awa.sepals.weight.result.WeightingResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

public class SepalsWeighting {
    public static <T> WeightingResult<T> getRandom(Random random, Catheter<T> pool, ToIntFunction<T> weightGetter) {
        return getRandom(random, pool, getWeightSum(pool, weightGetter), weightGetter);
    }

    public static <T> int getWeightSum(Catheter<T> pool, ToIntFunction<T> weightGetter) {
        LongReceptacle weight = new LongReceptacle(0L);

        pool.whenFoundFirst(obj -> weight.set(weight.get() + weightGetter.applyAsInt(obj)).get() >= 0, t -> t);


        if (weight.get() > 2147483647L) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        } else {
            return (int) weight.get();
        }
    }

    public static <T> WeightingResult<T> getRandom(Random random, Catheter<T> pool, int totalWeight, ToIntFunction<T> weightGetter) {
        if (totalWeight < 0) {
            throw Util.getFatalOrPause(new IllegalArgumentException("Negative total weight in getRandomItem"));
        } else if (totalWeight == 0) {
            return null;
        } else {
            int i = random.nextInt(totalWeight);
            return getAt(pool, i, weightGetter);
        }
    }

    public static <T> WeightingResult<T> getAt(Catheter<T> pool, int totalWeight, ToIntFunction<T> weightGetter) {
        IntegerReceptacle weight = new IntegerReceptacle(totalWeight);

        T result = pool.whenFoundFirst(obj -> weight.set(weight.get() - weightGetter.applyAsInt(obj)).get() >= 0, t -> t);

        return new WeightingResult<>(result, 0);
    }
}
