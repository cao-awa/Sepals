package com.github.cao.awa.sepals.mixin.registry.entry;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.block.state.BlockStateTagAccessor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(RegistryEntry.Reference.class)
public abstract class RegistryEntryReferenceMixin<T> implements BlockStateTagAccessor {
    @Unique
    private boolean sepals$isClimbableInitialized = false;

    @Unique
    private boolean sepals$isClimbable;

    @Override
    public boolean sepals$isClimbable() {
        return this.sepals$isClimbable;
    }

    @Override
    public boolean sepals$isClimbableInitialized() {
        return this.sepals$isClimbableInitialized;
    }

    @WrapOperation(
            method = "isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"
            )
    )
    @SuppressWarnings("unchecked")
    public boolean isIn(Set<TagKey<T>> instance, Object o, Operation<Boolean> original) {
        TagKey<T> tag = (TagKey<T>) o;

        if (Sepals.CONFIG.isEnableSepalsRegistryProbe() && tag == BlockTags.CLIMBABLE) {
            if (this.sepals$isClimbableInitialized) {
                return this.sepals$isClimbable;
            }

            this.sepals$isClimbable = original.call(instance, o);
            this.sepals$isClimbableInitialized = true;

            return this.sepals$isClimbable;
        } else {
            return original.call(instance, o);
        }
    }
}
