package com.github.cao.awa.sepals.entity.ai.task.look;

import com.github.cao.awa.sepals.entity.ai.cache.SepalsLivingTargetCache;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

import java.util.Optional;
import java.util.function.Predicate;

public class SepalsLookAtPlayerTask {
    public static SingleTickTask<LivingEntity> create(float maxDistance) {
        float f = maxDistance * maxDistance;
        return TaskTriggerer.task(
                context -> context.group(context.queryMemoryAbsent(MemoryModuleType.LOOK_TARGET), context.queryMemoryValue(MemoryModuleType.VISIBLE_MOBS))
                        .apply(
                                context,
                                (lookTarget, visibleMobs) -> (world, entity, time) -> {
                                    LivingTargetCache cache = context.getValue(visibleMobs);

                                    Predicate<LivingEntity> distance = target -> target.squaredDistanceTo(entity) <= f;

                                    Optional<? extends LivingEntity> optional;
                                    if (cache instanceof SepalsLivingTargetCache sepalsCache) {
                                        optional = entity.hasPassengers() ?
                                                sepalsCache.findFirstPlayer(distance::test, entity::hasPassenger) :
                                                sepalsCache.findFirstPlayer(distance::test);
                                    } else {
                                        optional = entity.hasPassengers() ?
                                                cache.findFirst(target -> distance.test(target) && !entity.hasPassenger(target)) :
                                                cache.findFirst(distance);
                                    }

                                    if (optional.isEmpty()) {
                                        return false;
                                    } else {
                                        lookTarget.remember(new EntityLookTarget(optional.get(), true));
                                        return true;
                                    }
                                }
                        )
        );
    }
}
