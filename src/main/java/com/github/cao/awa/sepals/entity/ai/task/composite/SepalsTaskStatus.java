package com.github.cao.awa.sepals.entity.ai.task.composite;

import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.Task;

public class SepalsTaskStatus {
    public static boolean isRunning(MultiTickTask.Status status) {
        return status == MultiTickTask.Status.RUNNING;
    }

    public static boolean isStopped(MultiTickTask.Status status) {
        return status == MultiTickTask.Status.STOPPED;
    }

    public static boolean isRunning(Task<?> task) {
        return isRunning(task.getStatus());
    }

    public static boolean isStopped(Task<?> task) {
        return isStopped(task.getStatus());
    }
}
