package com.github.cao.awa.sepals.entity.ai.task.composite;

import com.github.cao.awa.sepals.entity.ai.brain.DetailedDebuggableTask;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.Set;

public class SepalsCompositeSingleTask<E extends LivingEntity> implements Task<E>, DetailedDebuggableTask {
    private final Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState;
    private final Set<MemoryModuleType<?>> memoriesToForgetWhenStopped;
    private final Task<? super E> task;
    private MultiTickTask.Status status = MultiTickTask.Status.STOPPED;

    public SepalsCompositeSingleTask(
            Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState,
            Set<MemoryModuleType<?>> memoriesToForgetWhenStopped,
            Task<? super E> task
    ) {
        this.requiredMemoryState = requiredMemoryState;
        this.memoriesToForgetWhenStopped = memoriesToForgetWhenStopped;
        this.task = task;
    }

    @Override
    public MultiTickTask.Status getStatus() {
        return this.status;
    }

    private boolean shouldStart(E entity) {
        for (Map.Entry<MemoryModuleType<?>, MemoryModuleState> entry : this.requiredMemoryState.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            MemoryModuleState memoryModuleState = entry.getValue();
            if (!entity.getBrain().isMemoryInState(memoryModuleType, memoryModuleState)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public final boolean tryStarting(ServerWorld world, E entity, long time) {
        if (shouldStart(entity)) {
            this.status = MultiTickTask.Status.RUNNING;
            if (SepalsTaskStatus.isStopped(this.task)) {
                this.task.tryStarting(world, entity, time);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final void tick(ServerWorld world, E entity, long time) {
        if (SepalsTaskStatus.isRunning(this.task)) {
            this.task.tick(world, entity, time);
        }
        if (SepalsTaskStatus.isStopped(this.task)) {
            stop(world, entity, time);
        }
    }

    @Override
    public final void stop(ServerWorld world, E entity, long time) {
        this.status = MultiTickTask.Status.STOPPED;
        if (SepalsTaskStatus.isRunning(this.task)) {
            this.task.stop(world, entity, time);
        }
        this.memoriesToForgetWhenStopped.forEach(entity.getBrain()::forget);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public String toString() {
        return "(" + getName() + "): " + (SepalsTaskStatus.isRunning(this.task) ? this.task : "[]");
    }

    @Override
    public String information() {
        if (this.task instanceof DetailedDebuggableTask detailedTask) {
            return detailedTask.information();
        } else {
            return "SingleTask(delegate=" + this.task + ")";
        }
    }
}
