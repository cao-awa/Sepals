package com.github.cao.awa.sepals.entity.ai.task.look;

import com.github.cao.awa.sepals.entity.ai.cache.SepalsLivingTargetCache;
import com.github.cao.awa.sepals.entity.ai.task.SepalsSingleTickTask;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;
import java.util.function.Predicate;

public class SepalsLookAtPlayerTask extends SepalsSingleTickTask<LivingEntity> {
    private final float maxDistance;

    public SepalsLookAtPlayerTask(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public static SepalsLookAtPlayerTask create(float maxDistance) {
        return new SepalsLookAtPlayerTask(maxDistance);
    }

    @Override
    public boolean complete(ServerWorld world, LivingEntity entity, long time) {
        return require(MemoryModuleType.VISIBLE_MOBS, cache -> {
            Predicate<LivingEntity> distance = target -> target.squaredDistanceTo(entity) <= this.maxDistance;

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
                remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(optional.get(), true));
                return true;
            }
        });
    }

    @Override
    public String information() {
        return "LookAtPlayerTask(distance=" + this.maxDistance + ")";
    }
}
