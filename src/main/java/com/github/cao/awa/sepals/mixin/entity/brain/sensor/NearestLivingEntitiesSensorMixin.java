package com.github.cao.awa.sepals.mixin.entity.brain.sensor;

import com.github.cao.awa.sepals.Sepals;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.NearestLivingEntitiesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Comparator;
import java.util.List;

@Mixin(NearestLivingEntitiesSensor.class)
public class NearestLivingEntitiesSensorMixin {
    @Redirect(method = "sense", at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"))
    private void sense(List<LivingEntity> instance, Comparator<LivingEntity> comparator) {
        if (Sepals.nearestLivingEntitiesSensorUseQuickSort) {
            LivingEntity[] entities = instance.toArray(LivingEntity[]::new);
            ObjectArrays.parallelQuickSort(entities, comparator);
            instance.clear();
            instance.addAll(Lists.newArrayList(entities));
        } else {
            instance.sort(comparator);
        }
    }
}
