package com.github.cao.awa.sepals.entity.ai.task.composite;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.weight.WeightTable;
import com.github.cao.awa.sepals.weight.WeightedList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class SepalsRandomTask<E extends LivingEntity> extends SepalsCompositeTask<E> {
    public SepalsRandomTask(List<Pair<? extends Task<? super E>, Integer>> tasks) {
        this(ImmutableMap.of(), tasks);
    }

    public SepalsRandomTask(
            Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState,
            List<Pair<? extends Task<? super E>, Integer>> tasks
    ) {
        super(requiredMemoryState, ImmutableSet.of(), Order.SHUFFLED, RunMode.RUN_ONE, tasks);
    }

    @Override
    public final boolean tryStarting(ServerWorld world, E entity, long time) {
        if (shouldStart(entity)) {
            this.status = MultiTickTask.Status.RUNNING;
            this.tasks.shuffle();
            this.tasks.elements()
                    .filter(SepalsTaskStatus::isStopped)
                    .till(task -> task.tryStarting(world, entity, time));
            return true;
        } else {
            return false;
        }
    }
}
