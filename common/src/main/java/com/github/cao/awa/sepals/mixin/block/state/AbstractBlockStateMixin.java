package com.github.cao.awa.sepals.mixin.block.state;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.block.state.BlockStateTagAccessor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin implements BlockStateTagAccessor {
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

    @SuppressWarnings("all")
    @Redirect(
            method = "isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/registry/entry/RegistryEntry$Reference;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"
            )
    )
    public boolean isIn(RegistryEntry.Reference<Block> instance, TagKey<Block> tag) {
        if (Sepals.CONFIG.isEnableSepalsEntitiesCramming()) {
            if (this.sepals$isClimbableInitialized) {
                return this.sepals$isClimbable;
            }

            this.sepals$isClimbable = instance.isIn(tag);
            this.sepals$isClimbableInitialized = true;

            return this.sepals$isClimbable;
        } else {
            return instance.isIn(tag);
        }
    }
}
