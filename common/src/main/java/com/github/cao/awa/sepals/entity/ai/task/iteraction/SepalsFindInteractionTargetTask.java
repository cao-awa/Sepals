package com.github.cao.awa.sepals.entity.ai.task.iteraction;

import com.github.cao.awa.sepals.entity.ai.cache.SepalsLivingTargetCache;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

import java.util.Optional;

public class SepalsFindInteractionTargetTask {
    public static Task<LivingEntity> createTypedPlayer(int maxDistance) {
        double i = maxDistance * maxDistance;
        return TaskTriggerer.task(
                context -> context.group(
                                context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET),
                                context.queryMemoryAbsent(MemoryModuleType.INTERACTION_TARGET),
                                context.queryMemoryValue(MemoryModuleType.VISIBLE_MOBS)
                        )
                        .apply(
                                context,
                                (lookTarget, interactionTarget, visibleMobs) -> (world, entity, time) -> {
                                    LivingTargetCache targetCache = context.getValue(visibleMobs);

                                    Optional<? extends LivingEntity> optional;
                                    if (targetCache instanceof SepalsLivingTargetCache sepalsCache) {
                                        optional = sepalsCache.findFirstPlayer(target -> target.squaredDistanceTo(entity) <= i);
                                    } else {
                                        optional = targetCache.findFirst(target -> target.squaredDistanceTo(entity) <= i && EntityType.PLAYER.equals(target.getType()));
                                    }

                                    if (optional.isEmpty()) {
                                        return false;
                                    } else {
                                        LivingEntity livingEntity = optional.get();
                                        interactionTarget.remember(livingEntity);
                                        lookTarget.remember(new EntityLookTarget(livingEntity, true));
                                        return true;
                                    }
                                }
                        )
        );
    }
}
