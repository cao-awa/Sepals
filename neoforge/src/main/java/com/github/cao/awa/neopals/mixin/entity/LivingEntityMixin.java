package com.github.cao.awa.neopals.mixin.entity;

import com.github.cao.awa.sepals.block.BlockStateAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected abstract void pushAway(Entity entity);

    @Shadow public abstract void remove(RemovalReason reason);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(
            method = "isClimbing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/CommonHooks;isLivingOnLadder(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)Ljava/util/Optional;"
            )
    )
    public Optional<BlockPos> sepalsLivingEntityClibaleCache(BlockState state, World world, BlockPos pos, LivingEntity entity) {
//        Useless checks.

//        if (tagKey == BlockTags.CLIMBABLE) {
//            return ((BlockStateAccessor) instance).sepals$isClimbale();
//        } else {
//            return instance.isIn(BlockTags.CLIMBABLE)
//        }
//        return NeoForgeClimbing.isLivingOnLadder(state, world, pos, entity);
        return ((BlockStateAccessor) state).sepals$isClimbale() ? Optional.of(pos) : Optional.empty();
    }
}
