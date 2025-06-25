package com.github.cao.awa.neopals.climbing;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.neoforged.neoforge.common.config.NeoForgeServerConfig;

import java.util.Optional;

public class NeoForgeClimbing {
    public static Optional<BlockPos> isLivingOnLadder(BlockState state, World level, BlockPos pos, LivingEntity entity) {
        boolean isSpectator = (entity instanceof PlayerEntity && entity.isSpectator());
        if (isSpectator)
            return Optional.empty();
        if (!NeoForgeServerConfig.INSTANCE.fullBoundingBoxLadders.get()) {
            return state.isLadder(level, pos, entity) ? Optional.of(pos) : Optional.empty();
        } else {
            Box bb = entity.getBoundingBox();
            int mX = MathHelper.floor(bb.minX);
            int mY = MathHelper.floor(bb.minY);
            int mZ = MathHelper.floor(bb.minZ);
            for (int y2 = mY; y2 < bb.maxY; y2++) {
                for (int x2 = mX; x2 < bb.maxX; x2++) {
                    for (int z2 = mZ; z2 < bb.maxZ; z2++) {
                        BlockPos tmp = new BlockPos(x2, y2, z2);
                        state = level.getBlockState(tmp);
                        if (state.isLadder(level, tmp, entity)) {
                            return Optional.of(tmp);
                        }
                    }
                }
            }
            return Optional.empty();
        }
    }
}
