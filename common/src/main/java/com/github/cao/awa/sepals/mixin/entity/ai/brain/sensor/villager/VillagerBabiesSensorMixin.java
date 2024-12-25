package com.github.cao.awa.sepals.mixin.entity.ai.brain.sensor.villager;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.entity.ai.cache.SepalsLivingTargetCache;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.VillagerBabiesSensor;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VillagerBabiesSensor.class)
public abstract class VillagerBabiesSensorMixin {
    @Shadow protected abstract LivingTargetCache getVisibleMobs(LivingEntity entity);

    @Inject(
            method = "sense",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void sense(ServerWorld world, LivingEntity entity, CallbackInfo ci) {
        if (Sepals.CONFIG.isEnableSepalsVillager()) {
            entity.getBrain().remember(MemoryModuleType.VISIBLE_VILLAGER_BABIES, getVisibleVillagerBabies(entity));
            ci.cancel();
        }
    }

    @Unique
    private List<LivingEntity> getVisibleVillagerBabies(LivingEntity entity) {
        LivingTargetCache cache = getVisibleMobs(entity);
        if (cache instanceof SepalsLivingTargetCache sepalsCache) {
            return sepalsCache.collect(VillagerBabiesSensorMixin::isVillagerBaby);
        }
        return ImmutableList.copyOf(cache.iterate(VillagerBabiesSensorMixin::isVillagerBaby));
    }

    @Unique
    private static boolean isVillagerBaby(LivingEntity entity) {
        return entity.getType() == EntityType.VILLAGER && entity.isBaby();
    }
}
