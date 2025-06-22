package com.github.cao.awa.sepals.mixin.block;

import com.github.cao.awa.sepals.block.BlockAccessor;
import com.github.cao.awa.sepals.block.BlockStateAccessor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class BlockStateMixin implements BlockStateAccessor {
    @Shadow public abstract boolean isIn(TagKey<Block> tag);

    @Unique
    private boolean isBedsInitialized = false;
    @Unique
    private boolean isBeds = false;

    @Override
    public boolean sepals$isBed() {
        if (!this.isBedsInitialized) {
            this.isBeds = isIn(BlockTags.BEDS);
            this.isBedsInitialized = true;
        }

        return this.isBeds;
    }
}
