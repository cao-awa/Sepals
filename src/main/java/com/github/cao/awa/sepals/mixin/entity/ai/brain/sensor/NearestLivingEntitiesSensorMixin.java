package com.github.cao.awa.sepals.mixin.entity.ai.brain.sensor;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.entity.ai.cache.SepalsLivingTargetCache;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestLivingEntitiesSensor;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(NearestLivingEntitiesSensor.class)
public abstract class NearestLivingEntitiesSensorMixin<T extends LivingEntity> {
    @Shadow
    protected abstract int getHorizontalExpansion();

    @Shadow
    protected abstract int getHeightExpansion();

    @Inject(
            method = "sense",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void sense(ServerWorld world, T entity, CallbackInfo ci) {
        Box box = entity.getBoundingBox().expand(getHorizontalExpansion(), getHeightExpansion(), getHorizontalExpansion());
        Comparator<LivingEntity> comparator = Comparator.comparingDouble(entity::squaredDistanceTo);
        Brain<?> brain = entity.getBrain();

        List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, box, e -> e != entity && e.isAlive());
        LivingEntity[] entities = list.toArray(LivingEntity[]::new);

        if (Sepals.nearestLivingEntitiesSensorUseQuickSort) {
            ObjectArrays.parallelQuickSort(entities, comparator);
        } else {
            Arrays.sort(entities, comparator);
        }

        brain.remember(MemoryModuleType.MOBS, list);
        if (Sepals.enableSepalsLivingTargetCache) {
            brain.remember(MemoryModuleType.VISIBLE_MOBS, new SepalsLivingTargetCache(entity, entities));
        } else {
            brain.remember(MemoryModuleType.VISIBLE_MOBS, new LivingTargetCache(entity, Arrays.asList(entities)));
        }

        ci.cancel();
    }
}
