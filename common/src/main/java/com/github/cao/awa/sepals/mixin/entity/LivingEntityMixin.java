package com.github.cao.awa.sepals.mixin.entity;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.block.BlockStateAccessor;
import com.github.cao.awa.sepals.entity.intersects.SepalsWorldEntityIntersects;
import com.github.cao.awa.sepals.entity.predicate.SepalsEntityPredicates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected abstract void pushAway(Entity entity);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(
            method = "isClimbing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"
            )
    )
    protected boolean sepalsLivingEntityClibaleCache(BlockState instance, TagKey<Block> tagKey) {
//        Useless checks.

//        if (tagKey == BlockTags.CLIMBABLE) {
//            return ((BlockStateAccessor) instance).sepals$isClimbale();
//        } else {
//            return instance.isIn(BlockTags.CLIMBABLE)
//        }
        return ((BlockStateAccessor) instance).sepals$isClimbale();
    }

    @Inject(
            method = "tickCramming",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void sepalsForceCramming(CallbackInfo ci) {
        if (getWorld() instanceof ServerWorld serverWorld && Sepals.CONFIG.isEnableSepalsEntitiesCramming()) {
            int maxCramming = serverWorld.getGameRules().getInt(GameRules.MAX_ENTITY_CRAMMING);
            int crammingLimit = maxCramming - 1;

            Predicate <Entity> canBePushedByPredicate;

            if (Sepals.CONFIG.isEnableSepalsQuickCanBePushByEntityPredicate()) {
                canBePushedByPredicate = SepalsEntityPredicates.quickCanBePushedBy(this);
            } else {
                canBePushedByPredicate = EntityPredicates.canBePushedBy(this);
            }

            if (maxCramming > 0) {
                new SepalsWorldEntityIntersects().quickInterestOtherEntities(
                        serverWorld,
                        this,
                        getBoundingBox(),
                        canBePushedByPredicate,
                        this::pushAway,
                        (target) -> {
                            if (this.random.nextInt(4) == 0) {
                                damage(serverWorld, getDamageSources().cramming(), 6.0F);
                            }
                        },
                        crammingLimit
                );
            } else {
                new SepalsWorldEntityIntersects().quickInterestOtherEntities(
                        serverWorld,
                        this,
                        getBoundingBox(),
                        canBePushedByPredicate,
                        this::pushAway
                );
            }

            ci.cancel();
        }
    }
}
