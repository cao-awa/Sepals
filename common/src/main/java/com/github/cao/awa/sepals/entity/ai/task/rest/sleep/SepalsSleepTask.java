package com.github.cao.awa.sepals.entity.ai.task.rest.sleep;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SepalsSleepTask extends MultiTickTask<LivingEntity> {
    private long startTime;

    public SepalsSleepTask() {
        super(ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.LAST_WOKEN, MemoryModuleState.REGISTERED));
    }

    @Override
    protected boolean shouldRun(ServerWorld world, LivingEntity entity) {
        if (entity.hasVehicle()) {
            return false;
        } else {
            Brain<?> brain = entity.getBrain();
            GlobalPos globalPos = brain.getOptionalRegisteredMemory(MemoryModuleType.HOME).get();
            if (world.getRegistryKey() != globalPos.dimension()) {
                return false;
            } else {
                Optional<Long> optional = brain.getOptionalRegisteredMemory(MemoryModuleType.LAST_WOKEN);
                if (optional.isPresent()) {
                    long l = world.getTime() - optional.get();
                    if (l > 0L && l < 100L) {
                        return false;
                    }
                }

                BlockState blockState = world.getBlockState(globalPos.pos());

                return globalPos.pos().isWithinDistance(entity.getEntityPos(), 2.0) && blockState.isIn(BlockTags.BEDS) && !blockState.get(BedBlock.OCCUPIED);
            }
        }
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
        Optional<GlobalPos> optional = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HOME);
        if (optional.isEmpty()) {
            return false;
        } else {
            BlockPos blockPos = optional.get().pos();

            return entity.getBrain().hasActivity(Activity.REST) && entity.getY() > blockPos.getY() + 0.4 && blockPos.isWithinDistance(entity.getEntityPos(), 1.14);
        }
    }

    @Override
    protected void run(ServerWorld world, LivingEntity entity, long time) {
        if (time > this.startTime) {
            Brain<?> brain = entity.getBrain();
            if (brain.hasMemoryModule(MemoryModuleType.DOORS_TO_CLOSE)) {
                Set<GlobalPos> set = brain.getOptionalRegisteredMemory(MemoryModuleType.DOORS_TO_CLOSE).get();
                Optional<List<LivingEntity>> optional;
                if (brain.hasMemoryModule(MemoryModuleType.MOBS)) {
                    optional = brain.getOptionalRegisteredMemory(MemoryModuleType.MOBS);
                } else {
                    optional = Optional.empty();
                }

                OpenDoorsTask.pathToDoor(world, entity, null, null, set, optional);
            }

            entity.sleep(entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HOME).get().pos());
        }
    }

    @Override
    protected boolean isTimeLimitExceeded(long time) {
        return false;
    }

    @Override
    protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        if (entity.isSleeping()) {
            entity.wakeUp();
            this.startTime = time + 40L;
        }
    }
}
