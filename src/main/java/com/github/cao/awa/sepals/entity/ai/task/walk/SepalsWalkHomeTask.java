package com.github.cao.awa.sepals.entity.ai.task.walk;

import com.github.cao.awa.sepals.entity.ai.brain.DetailedDebuggableTask;
import com.github.cao.awa.sepals.entity.ai.task.SepalsSingleTickTask;
import com.github.cao.awa.sepals.entity.ai.task.poi.SepalsFindPointOfInterestTask;
import com.github.cao.awa.sepals.world.poi.SepalsPointOfInterestStorage;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class SepalsWalkHomeTask extends SepalsSingleTickTask<PathAwareEntity> implements DetailedDebuggableTask {
    private final float speed;
    private final Long2LongMap walkedPoses = new Long2LongOpenHashMap();
    private final MutableLong nextWalkTime = new MutableLong(0L);

    public SepalsWalkHomeTask(float speed) {
        this.speed = speed;
    }

    public static SepalsWalkHomeTask create(float speed) {
        return new SepalsWalkHomeTask(speed);
    }

    @Override
    public boolean complete(ServerWorld world, PathAwareEntity entity, long time) {
        if (world.getTime() - this.nextWalkTime.getValue() < 20L) {
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
                MutableInt limiter = new MutableInt(0);
                this.nextWalkTime.setValue(world.getTime() + world.getRandom().nextInt(20));
                Predicate<BlockPos> predicate = pos -> {
                    long l = pos.asLong();
                    if (this.walkedPoses.containsKey(l)) {
                        return false;
                    } else if (limiter.incrementAndGet() >= 5) {
                        return false;
                    } else {
                        this.walkedPoses.put(l, this.nextWalkTime.getValue() + 40L);
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
                    if (limiter.getValue() < 5) {
                        this.walkedPoses.long2LongEntrySet().removeIf(entry -> entry.getLongValue() < this.nextWalkTime.getValue());
                    }
                } else {
                    BlockPos blockPos = path.getTarget();
                    Optional<RegistryEntry<PointOfInterestType>> optional2 = pointOfInterestStorage.getType(blockPos);
                    if (optional2.isPresent()) {
                        remember(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, this.speed, 1));
                        DebugInfoSender.sendPointOfInterest(world, blockPos);
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public String information() {
        return "WalkHomeTask(nextWalk(waiting=" + (this.nextWalkTime.getValue() - currentTime()) + "), walked" + Arrays.toString(this.walkedPoses.values().longStream().toArray()) + ")";
    }
}
