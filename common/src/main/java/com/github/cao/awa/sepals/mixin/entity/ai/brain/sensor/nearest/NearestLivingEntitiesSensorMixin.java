package com.github.cao.awa.sepals.mixin.entity.ai.brain.sensor.nearest;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.entity.ai.cache.SepalsLivingTargetCache;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestLivingEntitiesSensor;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Mixin(NearestLivingEntitiesSensor.class)
public abstract class NearestLivingEntitiesSensorMixin<T extends LivingEntity> {
    @Inject(
            method = "sense",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void sense(ServerWorld world, T entity, CallbackInfo ci) {
        Box box = entity.getBoundingBox().expand(16.0D, 16.0D, 16.0D);

        Comparator<LivingEntity> comparator = Comparator.comparingDouble(entity::squaredDistanceTo);
        Brain<?> brain = entity.getBrain();

        List<LivingEntity> mobs = world.getEntitiesByClass(LivingEntity.class, box, livingEntity -> livingEntity != entity && livingEntity.isAlive());

        boolean useQuickSort = Sepals.CONFIG.isNearestLivingEntitiesSensorUseQuickSort();

        LivingTargetCache cache;

        if (Sepals.CONFIG.isEnableSepalsLivingTargetCache()) {
            Catheter<LivingEntity> entities = Catheter.of(mobs, LivingEntity[]::new);

            if (useQuickSort) {
                ObjectArrays.quickSort(entities.dArray(), comparator);
            } else {
                Arrays.sort(entities.dArray(), comparator);
            }

            LivingEntity[] sources = entities.array();

            mobs = ApricotCollectionFactor.arrayList(sources);

            PlayerEntity[] players = entities.filter(LivingEntity::isPlayer)
                    .varyTo(PlayerEntity.class::cast)
                    .arrayGenerator(PlayerEntity[]::new)
                    .safeArray();

            cache = new SepalsLivingTargetCache(
                    entity,
                    sources,
                    players
            );
        } else {
            if (useQuickSort) {
                LivingEntity[] entitiesArray = mobs.toArray(LivingEntity[]::new);
                ObjectArrays.quickSort(entitiesArray, comparator);
                mobs = Arrays.asList(entitiesArray);
            } else {
                mobs.sort(comparator);
            }
            cache = new LivingTargetCache(entity, mobs);
        }

        brain.remember(MemoryModuleType.MOBS, mobs);
        brain.remember(MemoryModuleType.VISIBLE_MOBS, cache);

        ci.cancel();
    }
}
