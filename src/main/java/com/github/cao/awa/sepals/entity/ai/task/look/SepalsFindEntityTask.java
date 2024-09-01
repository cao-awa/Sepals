package com.github.cao.awa.sepals.entity.ai.task.look;

import com.github.cao.awa.sepals.entity.ai.cache.SepalsLivingTargetCache;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

import java.util.Optional;
import java.util.function.Predicate;

public class SepalsFindEntityTask {
    public static <E extends LivingEntity> Task<LivingEntity> create(
            EntityType<? extends E> type, int maxDistance, MemoryModuleType<E> targetModule, float speed, int completionRange
    ) {
        return create(type, maxDistance, entity -> true, entity -> true, targetModule, speed, completionRange);
    }

    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity, E extends LivingEntity> Task<E> create(
            EntityType<? extends LivingEntity> type,
            int maxDistance,
            Predicate<E> entityPredicate,
            Predicate<T> targetPredicate,
            MemoryModuleType<T> targetModule,
            float speed,
            int completionRange
    ) {
        float f = maxDistance * maxDistance;
        Predicate<LivingEntity> predicate = entity -> type.equals(entity.getType()) && targetPredicate.test((T) entity);
        return TaskTriggerer.task(
                context -> context.group(
                                context.queryMemoryOptional(targetModule),
                                context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET),
                                context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET),
                                context.queryMemoryValue(MemoryModuleType.VISIBLE_MOBS)
                        )
                        .apply(context, (targetValue, lookTarget, walkTarget, visibleMobs) -> (world, entity, time) -> {
                            LivingTargetCache cache = context.getValue(visibleMobs);
                            if (entityPredicate.test(entity)) {

                                Optional<? extends LivingEntity> optional;
                                if (cache instanceof SepalsLivingTargetCache sepalsCache) {
                                    optional = sepalsCache.findFirst(target -> target.squaredDistanceTo(entity) <= f && predicate.test(target));
                                } else {
                                    optional = cache.findFirst(target -> target.squaredDistanceTo(entity) <= f && predicate.test(target));
                                }

                                optional.ifPresent(target -> {
                                    targetValue.remember((T) target);
                                    lookTarget.remember(new EntityLookTarget(target, true));
                                    walkTarget.remember(new WalkTarget(new EntityLookTarget(target, false), speed, completionRange));
                                });
                                return true;
                            } else {
                                return false;
                            }
                        })
        );
    }
}
