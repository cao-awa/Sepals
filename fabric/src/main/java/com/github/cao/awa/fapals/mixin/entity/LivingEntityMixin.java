package com.github.cao.awa.fapals.mixin.entity;

import com.github.cao.awa.sepals.block.BlockStateAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract void remove(RemovalReason reason);

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
    public boolean sepalsLivingEntityClibaleCache(BlockState instance, TagKey<Block> tagKey) {
//        Useless checks.

//        if (tagKey == BlockTags.CLIMBABLE) {
//            return ((BlockStateAccessor) instance).sepals$isClimbale();
//        } else {
//            return instance.isIn(BlockTags.CLIMBABLE)
//        }
        return ((BlockStateAccessor) instance).sepals$isClimbale();
    }
}
