package com.github.cao.awa.sepals.mixin.world.block;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.world.BlockViewAccessor;
import com.google.common.collect.AbstractIterator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;

@Mixin(BlockView.class)
public interface BlockViewMixin extends BlockViewAccessor {
    @Shadow
    BlockState getBlockState(BlockPos pos);

    @Unique
    default Iterator<BlockState> sepals$getStatesIteratorInBoxIfLoaded(Box box) {
        return sepals$getStatesIteratorInBox(box);
    }

    @Unique
    default Iterator<BlockState> sepals$getStatesIteratorInBox(Box box) {
        Iterator<BlockPos> itr = BlockPos.iterate(MathHelper.floor(box.minX),
                MathHelper.floor(box.minY),
                MathHelper.floor(box.minZ),
                MathHelper.floor(box.maxX),
                MathHelper.floor(box.maxY),
                MathHelper.floor(box.maxZ)
        ).iterator();

        return new AbstractIterator<>() {
            @Override
            protected BlockState computeNext() {
                if (itr.hasNext()) {
                    return getBlockState(itr.next());
                } else {
                    return endOfData();
                }
            }
        };
    }
}
