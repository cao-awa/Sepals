package com.github.cao.awa.sepals.entity.ai.task.look;

import com.github.cao.awa.sepals.entity.ai.cache.SepalsLivingTargetCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.Optional;

public class SepalsLookAtMobWithIntervalTask {
    public static Task<LivingEntity> frogFollow(float maxDistance, UniformIntProvider interval) {
        float f = maxDistance * maxDistance;
        LookAtMobWithIntervalTask.Interval interval2 = new LookAtMobWithIntervalTask.Interval(interval);
        return TaskTriggerer.task(
                context -> context.group(context.queryMemoryAbsent(MemoryModuleType.LOOK_TARGET), context.queryMemoryValue(MemoryModuleType.VISIBLE_MOBS))
                        .apply(
                                context,
                                (lookTarget, visibleMobs) -> (world, entity, time) -> {
                                    LivingTargetCache cache = context.getValue(visibleMobs);

                                    boolean usedSepals = false;
                                    Optional<LivingEntity> optional;
                                    if (cache instanceof SepalsLivingTargetCache sepalsCache) {
                                         optional = sepalsCache.findFirstPlayer(e -> e.squaredDistanceTo(entity) <= f, () -> interval2.shouldRun(world.random));
                                         usedSepals = true;
                                    } else {
                                        optional = cache.findFirst(e -> e.isPlayer() && e.squaredDistanceTo(entity) <= f);
                                    }

                                    if (optional.isEmpty()) {
                                        if (usedSepals || interval2.shouldRun(world.random)) {
                                            return false;
                                        }
                                        return false;
                                    }  else {
                                        lookTarget.remember(new EntityLookTarget(optional.get(), true));
                                        return true;
                                    }
                                }
                        )
        );
    }
}
