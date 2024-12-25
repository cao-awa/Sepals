package com.github.cao.awa.sepals.mixin.entity;

import com.github.cao.awa.sepals.Sepals;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected abstract void pushAway(Entity entity);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "tickCramming",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void sepalsForceCramming(CallbackInfo ci) {
        if (getWorld() instanceof ServerWorld serverWorld && Sepals.CONFIG.isEnableSepalsEntitiesCramming()) {
            List<Entity> list = getWorld().getOtherEntities(this, getBoundingBox(), EntityPredicates.canBePushedBy(this));
            if (!list.isEmpty()) {
                int maxCramming = serverWorld.getGameRules().getInt(GameRules.MAX_ENTITY_CRAMMING);
                if (maxCramming > 0 && list.size() > maxCramming - 1 && this.random.nextInt(4) == 0) {
                    crammingAndPushAway(serverWorld, maxCramming, list);
                } else {
                    onlyPushAway(list);
                }
            }

            ci.cancel();
        }
    }

    @Unique
    private void crammingAndPushAway(ServerWorld world, int maxCramming, List<Entity> list) {
        int cramming = 0;

        for (Entity entity : list) {
            if (!entity.hasVehicle()) {
                ++cramming;
            }
            pushAway(entity);
        }

        if (cramming > maxCramming - 1) {
            damage(world, getDamageSources().cramming(), 6.0F);
        }
    }

    @Unique
    private void onlyPushAway(List<Entity> list) {
        for (Entity entity : list) {
            pushAway(entity);
        }
    }
}
