package com.github.cao.awa.sepals.entity.ai.task.walk;

import com.github.cao.awa.sepals.entity.ai.task.poi.SepalsFindPointOfInterestTask;
import com.github.cao.awa.sepals.world.poi.SepalsPointOfInterestStorage;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.FindPointOfInterestTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SepalsWalkHomeTask {

    public static Task<PathAwareEntity> create(float speed) {
        Long2LongMap long2LongMap = new Long2LongOpenHashMap();
        MutableLong mutableLong = new MutableLong(0L);
        return TaskTriggerer.task(
                taskContext -> taskContext.group(taskContext.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), taskContext.queryMemoryAbsent(MemoryModuleType.HOME))
                        .apply(
                                taskContext,
                                (walkTarget, home) -> (world, entity, time) -> {
                                    if (world.getTime() - mutableLong.getValue() < 20L) {
                                        return false;
                                    } else {
                                        PointOfInterestStorage pointOfInterestStorage = world.getPointOfInterestStorage();

                                        Optional<BlockPos> optional = SepalsPointOfInterestStorage.getNearestPosition(
                                                pointOfInterestStorage,
                                                poiType -> poiType.matchesKey(PointOfInterestTypes.HOME),
                                                entity.getBlockPos(),
                                                48,
                                                PointOfInterestStorage.OccupationStatus.ANY
                                        );
                                        if (optional.isPresent() && optional.get().getSquaredDistance(entity.getBlockPos()) > 4.0) {
                                            MutableInt mutableInt = new MutableInt(0);
                                            mutableLong.setValue(world.getTime() + world.getRandom().nextInt(20));
                                            Predicate<BlockPos> predicate = pos -> {
                                                long l = pos.asLong();
                                                if (long2LongMap.containsKey(l)) {
                                                    return false;
                                                } else if (mutableInt.incrementAndGet() >= 5) {
                                                    return false;
                                                } else {
                                                    long2LongMap.put(l, mutableLong.getValue() + 40L);
                                                    return true;
                                                }
                                            };
                                            Pair<RegistryEntry<PointOfInterestType>, BlockPos>[] set = SepalsPointOfInterestStorage.getTypesAndPositions(
                                                            pointOfInterestStorage,
                                                            poiType -> poiType.matchesKey(PointOfInterestTypes.HOME),
                                                            predicate,
                                                            entity.getBlockPos(),
                                                            48,
                                                            PointOfInterestStorage.OccupationStatus.ANY
                                                    )
                                                    .safeArray();

                                            Path path = SepalsFindPointOfInterestTask.findPathToPoi(entity, set);

                                            if (path == null || !path.reachesTarget()) {
                                                if (mutableInt.getValue() < 5) {
                                                    long2LongMap.long2LongEntrySet().removeIf(entry -> entry.getLongValue() < mutableLong.getValue());
                                                }
                                            } else {
                                                BlockPos blockPos = path.getTarget();
                                                Optional<RegistryEntry<PointOfInterestType>> optional2 = pointOfInterestStorage.getType(blockPos);
                                                if (optional2.isPresent()) {
                                                    walkTarget.remember(new WalkTarget(blockPos, speed, 1));
                                                    DebugInfoSender.sendPointOfInterest(world, blockPos);
                                                }
                                            }

                                            return true;
                                        } else {
                                            return false;
                                        }
                                    }
                                }
                        )
        );
    }
}
