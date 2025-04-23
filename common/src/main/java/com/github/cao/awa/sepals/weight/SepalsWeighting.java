package com.github.cao.awa.sepals.weight;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.weight.result.WeightingResult;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class SepalsWeighting {
    public static <T> WeightingResult<T> getRandom(Random random, WeightTable.Ranged<T>[] weighted, int precalculated) {
        return new WeightTable<T>().initWeightWithPrecalculate(weighted, precalculated).selectRange(random);
    }
}
