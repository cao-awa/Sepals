package com.github.cao.awa.sepals.mixin.entity.ai.brain.villager;

import com.github.cao.awa.sepals.entity.ai.task.poi.SepalsFindPointOfInterestTask;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(VillagerTaskListProvider.class)
public class VillagerTaskListProviderMixin {
    @Inject(
            method = "createCoreTasks",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createCoreTasks(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        cir.setReturnValue(ImmutableList.of(
                Pair.of(0, new StayAboveWaterTask(0.8F)),
                Pair.of(0, OpenDoorsTask.create()),
                Pair.of(0, new LookAroundTask(45, 90)),
                Pair.of(0, new PanicTask()),
                Pair.of(0, WakeUpTask.create()),
                Pair.of(0, HideWhenBellRingsTask.create()),
                Pair.of(0, StartRaidTask.create()),
                Pair.of(0, ForgetCompletedPointOfInterestTask.create(profession.heldWorkstation(), MemoryModuleType.JOB_SITE)),
                Pair.of(0, ForgetCompletedPointOfInterestTask.create(profession.acquirableWorkstation(), MemoryModuleType.POTENTIAL_JOB_SITE)),
                Pair.of(1, new MoveToTargetTask()),
                Pair.of(2, WorkStationCompetitionTask.create()),
                Pair.of(3, new FollowCustomerTask(speed)),
                Pair.of(5, WalkToNearestVisibleWantedItemTask.create(speed, false, 4)),
                Pair.of(
                        6,
                        SepalsFindPointOfInterestTask.create(
                                profession.acquirableWorkstation(),
                                MemoryModuleType.JOB_SITE,
                                MemoryModuleType.POTENTIAL_JOB_SITE,
                                true,
                                null
                        )
                ),
                Pair.of(7, new WalkTowardJobSiteTask(speed)),
                Pair.of(8, TakeJobSiteTask.create(speed)),
                Pair.of(
                        10,
                        SepalsFindPointOfInterestTask.create(
                                poiType -> poiType.matchesKey(PointOfInterestTypes.HOME),
                                MemoryModuleType.HOME,
                                false,
                                (byte) 14
                        )
                ),
                Pair.of(
                        10,
                        SepalsFindPointOfInterestTask.create(
                                poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING),
                                MemoryModuleType.MEETING_POINT,
                                true,
                                (byte) 14
                        )
                ),
                Pair.of(10, GoToWorkTask.create()),
                Pair.of(10, LoseJobOnSiteLossTask.create())
        ));
        return;
    }
}
