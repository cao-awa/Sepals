package com.github.cao.awa.sepals.entity.ai.task.wait;

import com.github.cao.awa.sepals.entity.ai.brain.DetailedDebuggableTask;
import com.github.cao.awa.sepals.entity.ai.task.composite.SepalsTaskStatus;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.annotation.Debug;

public class SepalsWaitTask implements Task<LivingEntity>, DetailedDebuggableTask {
    private final int minRunTime;
    private final int maxRunTime;
    private MultiTickTask.Status status = MultiTickTask.Status.STOPPED;
    @Debug
    private long currentTime;
    private long waitUntil;

    public SepalsWaitTask(int minRunTime, int maxRunTime) {
        this.minRunTime = minRunTime;
        this.maxRunTime = maxRunTime;
    }

    @Override
    public MultiTickTask.Status getStatus() {
        return this.status;
    }

    @Override
    public final boolean tryStarting(ServerWorld world, LivingEntity entity, long time) {
        this.status = MultiTickTask.Status.RUNNING;
        int i = this.minRunTime + world.getRandom().nextInt(this.maxRunTime + 1 - this.minRunTime);
        this.waitUntil = time + i;
        return true;
    }

    @Override
    public final void tick(ServerWorld world, LivingEntity entity, long time) {
        this.currentTime = time;

        if (time > this.waitUntil) {
            stop(world, entity, time);
        }
    }

    @Override
    public final void stop(ServerWorld world, LivingEntity entity, long time) {
        this.status = MultiTickTask.Status.STOPPED;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String information() {
        if (SepalsTaskStatus.isStopped(this)) {
            return "WaitTask(STOPPED, runTime(max=" + this.maxRunTime + ", min=" + this.minRunTime + "))";
        } else {
            return "WaitTask(RUNNING, runTime(max=" + this.maxRunTime + ", min=" + this.minRunTime + ", remaining=" + (this.waitUntil - this.currentTime) + "))";
        }
    }
}
