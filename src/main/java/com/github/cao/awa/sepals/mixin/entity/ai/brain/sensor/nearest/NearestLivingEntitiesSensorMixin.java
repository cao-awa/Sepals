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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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

        List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, box, livingEntity -> livingEntity != entity && livingEntity.isAlive());

        if (Sepals.CONFIG.isEnableSepalsLivingTargetCache()) {
            if (Sepals.CONFIG.isNearestLivingEntitiesSensorUseQuickSort()) {
                LivingEntity[] entitiesArray = list.toArray(LivingEntity[]::new);
                ObjectArrays.quickSort(entitiesArray, comparator);
                list = Arrays.asList(entitiesArray);
            } else {
                list.sort(comparator);
            }
            brain.remember(MemoryModuleType.MOBS, list);
            brain.remember(MemoryModuleType.VISIBLE_MOBS, new LivingTargetCache(entity, list));
        } else {
            Catheter<LivingEntity> entities = Catheter.of(list, LivingEntity[]::new);

            if (Sepals.CONFIG.isNearestLivingEntitiesSensorUseQuickSort()) {
                ObjectArrays.quickSort(entities.dArray(), comparator);
            } else {
                Arrays.sort(entities.dArray(), comparator);
            }

            LivingEntity[] sources = entities.array();

            brain.remember(MemoryModuleType.MOBS, ApricotCollectionFactor.arrayList(sources));

            PlayerEntity[] players = entities.filter(LivingEntity::isPlayer)
                    .varyTo(PlayerEntity.class::cast)
                    .arrayGenerator(PlayerEntity[]::new)
                    .safeArray();

            brain.remember(MemoryModuleType.VISIBLE_MOBS, new SepalsLivingTargetCache(
                            entity,
                            sources,
                            players
                    )
            );
        }

        ci.cancel();
    }
}
