package com.github.cao.awa.sepals.entity.ai.task.wander;

import com.github.cao.awa.catheter.Catheter;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SepalsWanderIndoorsTask {
    public static Task<PathAwareEntity> create(float speed) {
        Random random = new Random();

        return TaskTriggerer.task(
                context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET))
                        .apply(
                                context,
                                walkTarget -> (world, entity, time) -> {
                                    if (world.isSkyVisible(entity.getBlockPos())) {
                                        return false;
                                    } else {
                                        if (world.isSpaceEmpty(entity)) {
                                            BlockPos blockPos = entity.getBlockPos();
                                            Catheter.of(BlockPos.stream(
                                                                    blockPos.add(-1, -1, -1),
                                                                    blockPos.add(1, 1, 1)
                                                            ).toArray(BlockPos[]::new)
                                                    )
                                                    .ifPresent(catheter -> ObjectArrays.shuffle(catheter.dArray(), random))
                                                    .whenAny(
                                                            pos -> !world.isSkyVisible(pos) && world.isTopSolid(pos, entity),
                                                            pos -> walkTarget.remember(new WalkTarget(pos.toImmutable(), speed, 0))
                                                    );
                                        }
                                        return true;
                                    }
                                }
                        )
        );
    }
}
