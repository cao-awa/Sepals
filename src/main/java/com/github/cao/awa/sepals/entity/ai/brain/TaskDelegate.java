package com.github.cao.awa.sepals.entity.ai.brain;

import com.github.cao.awa.catheter.Catheter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.Task;

public interface TaskDelegate<E extends LivingEntity> {
    Catheter<Task<? super E>> sepals$tasks();
}
