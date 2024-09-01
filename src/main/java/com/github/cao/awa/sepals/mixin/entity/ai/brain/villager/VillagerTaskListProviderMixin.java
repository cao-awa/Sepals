package com.github.cao.awa.sepals.mixin.entity.ai.brain.villager;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.entity.ai.task.composite.SepalsCompositeSingleTask;
import com.github.cao.awa.sepals.entity.ai.task.composite.SepalsRandomTask;
import com.github.cao.awa.sepals.entity.ai.task.iteraction.SepalsFindInteractionTargetTask;
import com.github.cao.awa.sepals.entity.ai.task.look.SepalsFindEntityTask;
import com.github.cao.awa.sepals.entity.ai.task.look.SepalsLookAtPlayerTask;
import com.github.cao.awa.sepals.entity.ai.task.poi.SepalsFindPointOfInterestTask;
import com.github.cao.awa.sepals.entity.ai.task.rest.SepalsWakeUpTask;
import com.github.cao.awa.sepals.entity.ai.task.schedule.SepalsScheduleActivityTask;
import com.github.cao.awa.sepals.entity.ai.task.sleep.SepalsSleepTask;
import com.github.cao.awa.sepals.entity.ai.task.wait.SepalsWaitTask;
import com.github.cao.awa.sepals.entity.ai.task.walk.SepalsWalkHomeTask;
import com.github.cao.awa.sepals.entity.ai.task.wander.SepalsWanderIndoorsTask;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTaskListProvider.class)
public abstract class VillagerTaskListProviderMixin {
    @Unique
    private static Pair<Integer, Task<LivingEntity>> createFreeFollowTask() {
        return Pair.of(
                5,
                new SepalsRandomTask<>(
                        ImmutableList.of(
                                Pair.of(LookAtMobTask.create(EntityType.CAT, 8.0F), 8),
                                Pair.of(LookAtMobTask.create(EntityType.VILLAGER, 8.0F), 2),
                                Pair.of(SepalsLookAtPlayerTask.create(8.0F), 2),
                                Pair.of(LookAtMobTask.create(SpawnGroup.CREATURE, 8.0F), 1),
                                Pair.of(LookAtMobTask.create(SpawnGroup.WATER_CREATURE, 8.0F), 1),
                                Pair.of(LookAtMobTask.create(SpawnGroup.AXOLOTLS, 8.0F), 1),
                                Pair.of(LookAtMobTask.create(SpawnGroup.UNDERGROUND_WATER_CREATURE, 8.0F), 1),
                                Pair.of(LookAtMobTask.create(SpawnGroup.WATER_AMBIENT, 8.0F), 1),
                                Pair.of(LookAtMobTask.create(SpawnGroup.MONSTER, 8.0F), 1),
                                Pair.of(new SepalsWaitTask(30, 60), 2)
                        )
                )
        );
    }

    @Unique
    private static Pair<Integer, Task<LivingEntity>> createBusyFollowTask() {
        return Pair.of(
                5,
                new SepalsRandomTask<>(
                        ImmutableList.of(
                                Pair.of(LookAtMobTask.create(EntityType.VILLAGER, 8.0F), 2),
                                Pair.of(SepalsLookAtPlayerTask.create(8.0F), 2),
                                Pair.of(new SepalsWaitTask(30, 60), 8)
                        )
                )
        );
    }

    @Inject(
            method = "createCoreTasks",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createCoreTasks(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (Sepals.enableSepalsVillager) {
            cir.setReturnValue(ImmutableList.of(
                    Pair.of(0, new StayAboveWaterTask(0.8F)),
                    Pair.of(0, OpenDoorsTask.create()),
                    Pair.of(0, new LookAroundTask(45, 90)),
                    Pair.of(0, new PanicTask()),
                    Pair.of(0, SepalsWakeUpTask.create()),
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
        }
    }

    @Inject(
            method = "createMeetTasks",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createMeetTasks(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (Sepals.enableSepalsVillager) {
            cir.setReturnValue(ImmutableList.of(
                    Pair.of(
                            2,
                            Tasks.pickRandomly(ImmutableList.of(Pair.of(GoToIfNearbyTask.create(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2), Pair.of(MeetVillagerTask.create(), 2)))
                    ),
                    Pair.of(10, new HoldTradeOffersTask(400, 1600)),
                    Pair.of(10, SepalsFindInteractionTargetTask.createTypedPlayer(4)),
                    Pair.of(2, VillagerWalkTowardsTask.create(MemoryModuleType.MEETING_POINT, speed, 6, 100, 200)),
                    Pair.of(3, new GiveGiftsToHeroTask(100)),
                    Pair.of(3, ForgetCompletedPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING), MemoryModuleType.MEETING_POINT)),
                    Pair.of(
                            3,
                            new SepalsCompositeSingleTask<>(
                                    ImmutableMap.of(),
                                    ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                                    new GatherItemsVillagerTask()
                            )
                    ),
                    createFreeFollowTask(),
                    Pair.of(99, new SepalsScheduleActivityTask()))
            );
        }
    }

    @Inject(
            method = "createWorkTasks",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createWorkTasks(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (Sepals.enableSepalsVillager) {
            VillagerWorkTask villagerWorkTask;
            if (profession == VillagerProfession.FARMER) {
                villagerWorkTask = new FarmerWorkTask();
            } else {
                villagerWorkTask = new VillagerWorkTask();
            }

            cir.setReturnValue(ImmutableList.of(
                    createBusyFollowTask(),
                    Pair.of(
                            5,
                            new SepalsRandomTask<>(
                                    ImmutableList.of(
                                            Pair.of(villagerWorkTask, 7),
                                            Pair.of(GoToIfNearbyTask.create(MemoryModuleType.JOB_SITE, 0.4F, 4), 2),
                                            Pair.of(GoToNearbyPositionTask.create(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
                                            Pair.of(GoToSecondaryPositionTask.create(MemoryModuleType.SECONDARY_JOB_SITE, speed, 1, 6, MemoryModuleType.JOB_SITE), 5),
                                            Pair.of(new FarmerVillagerTask(), profession == VillagerProfession.FARMER ? 2 : 5),
                                            Pair.of(new BoneMealTask(), profession == VillagerProfession.FARMER ? 4 : 7)
                                    )
                            )
                    ),
                    Pair.of(10, new HoldTradeOffersTask(400, 1600)),
                    Pair.of(10, SepalsFindInteractionTargetTask.createTypedPlayer(4)),
                    Pair.of(2, VillagerWalkTowardsTask.create(MemoryModuleType.JOB_SITE, speed, 9, 100, 1200)),
                    Pair.of(3, new GiveGiftsToHeroTask(100)),
                    Pair.of(99, new SepalsScheduleActivityTask())
            ));
        }
    }

    @Inject(
            method = "createIdleTasks",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createIdleTasks(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (Sepals.enableSepalsVillager) {
            cir.setReturnValue(ImmutableList.of(
                    Pair.of(
                            2,
                            new SepalsRandomTask<>(
                                    ImmutableList.of(
                                            Pair.of(SepalsFindEntityTask.create(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 2),
                                            Pair.of(
                                                    SepalsFindEntityTask.create(EntityType.VILLAGER, 8, PassiveEntity::isReadyToBreed, PassiveEntity::isReadyToBreed, MemoryModuleType.BREED_TARGET, speed, 2), 1
                                            ),
                                            Pair.of(SepalsFindEntityTask.create(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 1),
                                            Pair.of(FindWalkTargetTask.create(speed), 1),
                                            Pair.of(GoTowardsLookTargetTask.create(speed, 2), 1),
                                            Pair.of(new JumpInBedTask(speed), 1),
                                            Pair.of(new SepalsWaitTask(30, 60), 1)
                                    )
                            )
                    ),
                    Pair.of(3, new GiveGiftsToHeroTask(100)),
                    Pair.of(3, SepalsFindInteractionTargetTask.createTypedPlayer(4)),
                    Pair.of(3, new HoldTradeOffersTask(400, 1600)),
                    Pair.of(
                            3,
                            new SepalsCompositeSingleTask<>(
                                    ImmutableMap.of(),
                                    ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                                    new GatherItemsVillagerTask()
                            )
                    ),
                    Pair.of(
                            3,
                            new SepalsCompositeSingleTask<>(
                                    ImmutableMap.of(),
                                    ImmutableSet.of(MemoryModuleType.BREED_TARGET),
                                    new VillagerBreedTask()
                            )
                    ),
                    createFreeFollowTask(),
                    Pair.of(99, new SepalsScheduleActivityTask())
            ));
        }
    }

    @Inject(
            method = "createPanicTasks",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createPanicTasks(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (Sepals.enableSepalsVillager) {
            float f = speed * 1.5f;
            cir.setReturnValue(
                    ImmutableList.of(
                            Pair.of(0, StopPanickingTask.create()),
                            Pair.of(1, GoToRememberedPositionTask.createEntityBased(MemoryModuleType.NEAREST_HOSTILE, f, 6, false)),
                            Pair.of(1, GoToRememberedPositionTask.createEntityBased(MemoryModuleType.HURT_BY_ENTITY, f, 6, false)),
                            Pair.of(3, FindWalkTargetTask.create(f, 2, 2)),
                            createBusyFollowTask()
                    ));
        }
    }

    @Inject(
            method = "createPlayTasks",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createPlayTasks(float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (Sepals.enableSepalsVillager) {
            cir.setReturnValue(ImmutableList.of(
                    Pair.of(0, new MoveToTargetTask(80, 120)),
                    createFreeFollowTask(),
                    Pair.of(5, PlayWithVillagerBabiesTask.create()),
                    Pair.of(
                            5,
                            new SepalsRandomTask<>(
                                    ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleState.VALUE_ABSENT),
                                    ImmutableList.of(
                                            Pair.of(SepalsFindEntityTask.create(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 2),
                                            Pair.of(SepalsFindEntityTask.create(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 1),
                                            Pair.of(FindWalkTargetTask.create(speed), 1),
                                            Pair.of(GoTowardsLookTargetTask.create(speed, 2), 1),
                                            Pair.of(new JumpInBedTask(speed), 2),
                                            Pair.of(new SepalsWaitTask(20, 40), 2)
                                    )
                            )
                    ),
                    Pair.of(99, new SepalsScheduleActivityTask())
            ));
        }
    }

    @Inject(
            method = "createRestTasks",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createRestTasks(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (Sepals.enableSepalsVillager) {
            cir.setReturnValue(ImmutableList.of(
                    Pair.of(2, VillagerWalkTowardsTask.create(MemoryModuleType.HOME, speed, 1, 150, 1200)),
                    Pair.of(3, ForgetCompletedPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.HOME), MemoryModuleType.HOME)),
                    Pair.of(3, new SepalsSleepTask()),
                    Pair.of(
                            5,
                            new SepalsRandomTask<>(
                                    ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleState.VALUE_ABSENT),
                                    ImmutableList.of(
                                            Pair.of(SepalsWalkHomeTask.create(speed), 1),
                                            Pair.of(SepalsWanderIndoorsTask.create(speed), 4),
                                            Pair.of(GoToPointOfInterestTask.create(speed, 4), 2),
                                            Pair.of(new SepalsWaitTask(20, 40), 2)
                                    )
                            )
                    ),
                    createBusyFollowTask(),
                    Pair.of(99, new SepalsScheduleActivityTask())
            ));
        }
    }

    @Inject(
            method = "createFreeFollowTask",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createFreeFollowTask(CallbackInfoReturnable<Pair<Integer, Task<LivingEntity>>> cir) {
        if (Sepals.enableSepalsVillager) {
            cir.setReturnValue(Pair.of(
                    5,
                    new SepalsRandomTask<>(
                            ImmutableList.of(
                                    Pair.of(LookAtMobTask.create(EntityType.CAT, 8.0F), 8),
                                    Pair.of(LookAtMobTask.create(EntityType.VILLAGER, 8.0F), 2),
                                    Pair.of(SepalsLookAtPlayerTask.create(8.0F), 2),
                                    Pair.of(LookAtMobTask.create(SpawnGroup.CREATURE, 8.0F), 1),
                                    Pair.of(LookAtMobTask.create(SpawnGroup.WATER_CREATURE, 8.0F), 1),
                                    Pair.of(LookAtMobTask.create(SpawnGroup.AXOLOTLS, 8.0F), 1),
                                    Pair.of(LookAtMobTask.create(SpawnGroup.UNDERGROUND_WATER_CREATURE, 8.0F), 1),
                                    Pair.of(LookAtMobTask.create(SpawnGroup.WATER_AMBIENT, 8.0F), 1),
                                    Pair.of(LookAtMobTask.create(SpawnGroup.MONSTER, 8.0F), 1),
                                    Pair.of(new SepalsWaitTask(30, 60), 2)
                            )
                    )
            ));
        }
    }

    @Inject(
            method = "createBusyFollowTask",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createBusyFollowTask(CallbackInfoReturnable<Pair<Integer, Task<LivingEntity>>> cir) {
        if (Sepals.enableSepalsVillager) {
            cir.setReturnValue(Pair.of(
                    5,
                    new SepalsRandomTask<>(
                            ImmutableList.of(
                                    Pair.of(LookAtMobTask.create(EntityType.VILLAGER, 8.0F), 2),
                                    Pair.of(SepalsLookAtPlayerTask.create(8.0F), 2),
                                    Pair.of(new SepalsWaitTask(30, 60), 8)
                            )
                    )
            ));
        }
    }
}
