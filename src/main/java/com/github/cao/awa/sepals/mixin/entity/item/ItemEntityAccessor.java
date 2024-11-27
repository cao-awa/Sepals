package com.github.cao.awa.sepals.mixin.entity.item;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Invoker
    boolean invokeCanMerge();
}
