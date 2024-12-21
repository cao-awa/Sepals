package com.github.cao.awa.sepals.entity.ai.brain.frog;

import com.github.cao.awa.sepals.entity.ai.task.SepalsLongJumpTask;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

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
                frog, posDownState, nodeType
        )) {
            return true;
        }

        boolean nodeTypeIsTrapdoor1 = nodeType == PathNodeType.TRAPDOOR;

        return posDownStateIsAir ? nodeTypeIsTrapdoor1 :
                nodeTypeIsTrapdoor1 || LandPathNodeMaker.getLandNodeType(
                        pathContext, posDown.mutableCopy()
                ) == PathNodeType.TRAPDOOR;
    }

    public static boolean attackable(ServerWorld world, LivingEntity entity, LivingEntity target) {
        if (isHuntingCooldown(entity) || !FrogEntity.isValidFrogFood(target)) {
            return false;
        }

        if (!target.isInRange(entity, 10.0)) {
            return false;
        }

        return !isTargetUnreachable(entity, target) && Sensor.testAttackableTargetPredicate(world, entity, target);
    }

    private static boolean isHuntingCooldown(LivingEntity entity) {
        return entity.getBrain().hasMemoryModule(MemoryModuleType.HAS_HUNTING_COOLDOWN);
    }

    private static boolean isTargetUnreachable(LivingEntity entity, LivingEntity target) {
        return entity.getBrain()
                .getOptionalRegisteredMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS)
                .orElseGet(ArrayList::new)
                .contains(target.getUuid());
    }
}
