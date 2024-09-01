package com.github.cao.awa.sepals.entity.ai.task.rest;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskRunnable;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

public class SepalsWakeUpTask {
    public static Task<LivingEntity> create() {
        return TaskTriggerer.task(context -> context.point((world, entity, time) -> {
            if (!entity.isSleeping() || entity.getBrain().hasActivity(Activity.REST)) {
                return false;
            } else {
                entity.wakeUp();
                return true;
            }
        }));
    }
}
