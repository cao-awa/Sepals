package com.github.cao.awa.sepals.entity.ai.task.schedule;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.server.world.ServerWorld;

public class SepalsScheduleActivityTask extends SingleTickTask<LivingEntity> {
    @Override
    public boolean trigger(ServerWorld world, LivingEntity entity, long time) {
        entity.getBrain().refreshActivities(world.getTimeOfDay(), world.getTime());
        return true;
    }
}
