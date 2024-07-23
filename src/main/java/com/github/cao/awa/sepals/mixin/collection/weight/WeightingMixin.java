package com.github.cao.awa.sepals.mixin.collection.weight;

import com.github.cao.awa.apricot.annotations.Stable;
import com.github.cao.awa.sepals.weight.WeightTable;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Stable
@Mixin(Weighting.class)
public class WeightingMixin {
    /**
     * Replaced vanilla weighted random to sepals implement.
     *
     * @param random a minecraft random generator
     * @param pool weighted elements
     * @param cir mixin callback
     * @param <T> weighted type
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    @Inject(
            method = "getRandom(Lnet/minecraft/util/math/random/Random;Ljava/util/List;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static <T extends Weighted> void getRandom(Random random, List<T> pool, CallbackInfoReturnable<Optional<T>> cir) {
        T result = new WeightTable<T>().initWeight(pool).select(random);

        if (result == null) {
            cir.setReturnValue(Optional.empty());
            return;
        }

        cir.setReturnValue(Optional.of(result));
    }
}
