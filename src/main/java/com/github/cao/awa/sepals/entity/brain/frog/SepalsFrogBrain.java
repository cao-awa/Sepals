package com.github.cao.awa.sepals.entity.brain.frog;

import com.github.cao.awa.sepals.entity.task.SepalsLongJumpTask;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SepalsFrogBrain {
    public static <E extends MobEntity> boolean shouldJumpTo(E frog, BlockPos pos) {
        World world = frog.getWorld();

        BlockState posState = world.getBlockState(pos);
        boolean posStateIsAir = posState.isAir();
        if (posStateIsAir) {
            return true;
        }

        BlockPos posDown = pos.down();
        BlockState posDownState = world.getBlockState(posDown);
        boolean posDownStateIsAir = posDownState.isAir();
        boolean posDownStateNotAir = !posDownStateIsAir;

        if (posState.isIn(BlockTags.FROG_PREFER_JUMP_TO) || (!posDownStateNotAir && posDownState.isIn(BlockTags.FROG_PREFER_JUMP_TO))) {
            return true;
        }

        if (!posState.getFluidState().isEmpty() || (posDownStateNotAir && !posDownState.getFluidState().isEmpty()) || !world.getFluidState(pos.up()).isEmpty()) {
            return false;
        }

        PathContext pathContext = new PathContext(world, frog);
        PathNodeType nodeType = LandPathNodeMaker.getLandNodeType(
                pathContext, pos.mutableCopy()
        );

        if (SepalsLongJumpTask.shouldJumpTo(
                world, frog, posDown, posDownState, nodeType
        )) {
            return true;
        }

        boolean nodeTypeIsTrapdoor1 = nodeType == PathNodeType.TRAPDOOR;

        return posDownStateIsAir ? nodeTypeIsTrapdoor1 :
                nodeTypeIsTrapdoor1 || LandPathNodeMaker.getLandNodeType(
                        pathContext, posDown.mutableCopy()
                ) == PathNodeType.TRAPDOOR;
    }
}
