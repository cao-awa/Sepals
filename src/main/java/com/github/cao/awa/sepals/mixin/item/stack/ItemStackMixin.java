package com.github.cao.awa.sepals.mixin.item.stack;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {
//    @Unique
//    private int maxCount = -1;
//    @Unique
//    private boolean maxCountCached = false;
//
//    @Inject(
//            method = "getMaxCount",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    public void cacheMaxCount(CallbackInfoReturnable<Integer> cir) {
//        if (!this.maxCountCached) {
//            this.maxCount = getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
//            this.maxCountCached = true;
//        }
//        cir.setReturnValue(this.maxCount);
//    }
//
//    @Inject(
//            method = "set",
//            at = @At("HEAD")
//    )
//    public void setMaxCount(ComponentType<?> type, @Nullable Object value, CallbackInfoReturnable<Object> cir) {
//        if (type == DataComponentTypes.MAX_STACK_SIZE) {
//            this.maxCount = ((Integer) value);
//        }
//    }
}
