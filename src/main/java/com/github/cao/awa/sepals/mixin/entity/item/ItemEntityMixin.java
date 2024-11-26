package com.github.cao.awa.sepals.mixin.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO preparing to optimization items.
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
//    @Unique
//    private boolean isFireImmuneCached = false;
//    @Unique
//    private boolean fireImmune = false;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

//    @Shadow
//    protected abstract boolean canMerge();
//
////    @Redirect(
////            method = "tick",
////            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;canMerge()Z")
////    )
////    private boolean spacingMergeSkipping(ItemEntity instance) {
////        return isOnGround() && canMerge();
////    }
//
//    @Inject(
//            method = "isFireImmune",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void fireImmune(CallbackInfoReturnable<Boolean> cir) {
//        if (this.isFireImmuneCached) {
//            cir.setReturnValue(this.fireImmune);
//        }
//    }
//
//    @Inject(
//            method = "isFireImmune",
//            at = @At("RETURN")
//    )
//    private void cacheFireImmune(CallbackInfoReturnable<Boolean> cir) {
//        this.isFireImmuneCached = true;
//        this.fireImmune = cir.getReturnValue();
//    }
}
