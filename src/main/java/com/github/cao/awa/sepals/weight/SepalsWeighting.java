package com.github.cao.awa.sepals.weight;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.weight.result.WeightingResult;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class SepalsWeighting {
    public static <T extends Weighted> WeightingResult<T> getRandom(Random random, List<T> pool) {
        return new WeightTable<T>().initWeight(pool).selectWithIndex(random);
    }

    public static <T extends Weighted> WeightingResult<T> getRandom(Random random, Catheter<T> pool) {
        return new WeightTable<T>().initWeight(pool).selectWithIndex(random);
    }

    public static <T extends Weighted> WeightingResult<T> getRandom(Random random, WeightTable.Ranged<T>[] weighted, int precalculated) {
        return new WeightTable<T>().initWeightWithPrecalculate(weighted, precalculated).selectWithIndex(random);
    }

    public static <T extends Weighted> T getRandomDirect(Random random, List<T> pool) {
        return new WeightTable<T>().initWeight(pool).select(random);
    }

    public static <T extends Weighted> T getRandomDirect(Random random, Catheter<T> pool) {
        return new WeightTable<T>().initWeight(pool).select(random);
    }

    public static <T extends Weighted> T getRandomDirect(Random random, WeightTable.Ranged<T>[] weighted, int precalculated) {
        return new WeightTable<T>().initWeightWithPrecalculate(weighted, precalculated).select(random);
    }
}
