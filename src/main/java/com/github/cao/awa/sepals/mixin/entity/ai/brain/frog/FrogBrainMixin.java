package com.github.cao.awa.sepals.mixin.entity.ai.brain.frog;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.entity.ai.brain.frog.SepalsFrogBrain;
import com.github.cao.awa.sepals.entity.ai.task.biased.SepalsBiasedLongJumpTask;
import com.github.cao.awa.sepals.entity.ai.task.look.SepalsLookAtMobWithIntervalTask;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.FrogBrain;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FrogBrain.class)
public abstract class FrogBrainMixin {
    @Shadow
    @Final
    private static UniformIntProvider longJumpCooldownRange;

    @Inject(
            method = "addLongJumpActivities",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void addLongJumpActivities(Brain<FrogEntity> brain, CallbackInfo ci) {
        if (Sepals.enableSepalsBiasedJumpLongTask) {
            brain.setTaskList(
                    Activity.LONG_JUMP,
                    ImmutableList.of(
                            Pair.of(0, new LeapingChargeTask(longJumpCooldownRange, SoundEvents.ENTITY_FROG_STEP)),
                            Pair.of(
                                    1,
                                    new SepalsBiasedLongJumpTask<>(
                                            longJumpCooldownRange,
                                            2,
                                            4,
                                            3.5714288F,
                                            frog -> SoundEvents.ENTITY_FROG_LONG_JUMP,
                                            BlockTags.FROG_PREFER_JUMP_TO,
                                            0.5F,
                                            SepalsFrogBrain::shouldJumpTo
                                    )
                            )
                    ),
                    ImmutableSet.of(
                            Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleState.VALUE_ABSENT),
                            Pair.of(MemoryModuleType.BREED_TARGET, MemoryModuleState.VALUE_ABSENT),
                            Pair.of(MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleState.VALUE_ABSENT),
                            Pair.of(MemoryModuleType.IS_IN_WATER, MemoryModuleState.VALUE_ABSENT)
                    )
            );

            ci.cancel();
            return;
        }
    }

    @Redirect(
            method = "addIdleActivities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/LookAtMobWithIntervalTask;follow(Lnet/minecraft/entity/EntityType;FLnet/minecraft/util/math/intprovider/UniformIntProvider;)Lnet/minecraft/entity/ai/brain/task/Task;"
            )
    )
    private static Task<LivingEntity> addIdleActivities(EntityType<?> type, float maxDistance, UniformIntProvider interval) {
        if (Sepals.enableSepalsFrogLookAt) {
            return SepalsLookAtMobWithIntervalTask.frogFollow(6.0f, UniformIntProvider.create(30, 60));
        }
        return LookAtMobWithIntervalTask.follow(type, maxDistance, interval);
    }

    @Redirect(
            method = "addSwimActivities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/LookAtMobWithIntervalTask;follow(Lnet/minecraft/entity/EntityType;FLnet/minecraft/util/math/intprovider/UniformIntProvider;)Lnet/minecraft/entity/ai/brain/task/Task;"
            )
    )
    private static Task<LivingEntity> addSwimActivities(EntityType<?> type, float maxDistance, UniformIntProvider interval) {
        if (Sepals.enableSepalsFrogLookAt) {
            return SepalsLookAtMobWithIntervalTask.frogFollow(6.0f, UniformIntProvider.create(30, 60));
        }
        return LookAtMobWithIntervalTask.follow(type, maxDistance, interval);
    }

    @Redirect(
            method = "addLaySpawnActivities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/LookAtMobWithIntervalTask;follow(Lnet/minecraft/entity/EntityType;FLnet/minecraft/util/math/intprovider/UniformIntProvider;)Lnet/minecraft/entity/ai/brain/task/Task;"
            )
    )
    private static Task<LivingEntity> addLaySpawnActivities(EntityType<?> type, float maxDistance, UniformIntProvider interval) {
        if (Sepals.enableSepalsFrogLookAt) {
            return SepalsLookAtMobWithIntervalTask.frogFollow(6.0f, UniformIntProvider.create(30, 60));
        }
        return LookAtMobWithIntervalTask.follow(type, maxDistance, interval);
    }
}
