package com.github.cao.awa.sepals.entity.ai.task;

import com.github.cao.awa.sepals.entity.ai.brain.DetailedDebuggableTask;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.annotation.Debug;

import java.util.function.Predicate;

public abstract class SepalsSingleTickTask<E extends LivingEntity> extends SingleTickTask<E> implements DetailedDebuggableTask {
    private ServerWorld currentWorld;
    private E currentEntity;
    private Brain<?> currentBrain;
    @Debug
    private long currentTime;

    @Override
    public boolean trigger(ServerWorld world, E entity, long time) {
        this.currentWorld = world;
        this.currentEntity = entity;
        this.currentBrain = entity.getBrain();
        this.currentTime = time;
        return complete(world, entity, time);
    }

    public abstract boolean complete(ServerWorld world, E entity, long time);

    public <U> boolean require(MemoryModuleType<U> memoryType, Predicate<U> action) {
        return currentBrain().getOptionalRegisteredMemory(memoryType).filter(action).isPresent();
    }

    public <U> void remember(MemoryModuleType<U> memoryType, U value) {
        currentBrain().remember(memoryType, value);
    }

    public ServerWorld currentWorld() {
        return this.currentWorld;
    }

    public E currentEntity() {
        return this.currentEntity;
    }

    public Brain<?> currentBrain() {
        return this.currentBrain;
    }

    public long currentTime() {
        return this.currentTime;
    }

    @Override
    public boolean alwaysRunning() {
        return true;
    }

    @Override
    public String toString() {
        return information();
    }
}
