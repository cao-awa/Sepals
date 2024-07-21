package com.github.cao.awa.sepals.mixin.collection;

import net.minecraft.util.collection.TypeFilterableList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TypeFilterableList.class)
public interface TypeFilterableListAccessor<T> {
    @Accessor
    List<T> getAllElements();
}
