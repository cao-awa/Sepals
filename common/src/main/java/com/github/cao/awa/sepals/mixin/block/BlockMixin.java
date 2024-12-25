package com.github.cao.awa.sepals.mixin.block;

import com.github.cao.awa.sepals.block.BlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public abstract class BlockMixin implements BlockAccessor {
    @Shadow @Deprecated public abstract RegistryEntry.Reference<Block> getRegistryEntry();

    @Unique
    private boolean isLavaInitialized = false;
    @Unique
    private boolean isLava = false;
    @Unique
    private boolean isFireInitialized = false;
    @Unique
    private boolean isFire = false;

    @Override
    public boolean sepals$isLava() {
        if (!this.isLavaInitialized) {
            this.isLava = (Object) this == Blocks.LAVA;
            this.isLavaInitialized = true;
        }

        return this.isLava;
    }

    @Override
    public boolean sepals$isFire() {
        if (!this.isFireInitialized) {
            this.isFire = getRegistryEntry().isIn(BlockTags.FIRE);
            this.isFireInitialized = true;
        }
        return this.isFire;
    }
}
