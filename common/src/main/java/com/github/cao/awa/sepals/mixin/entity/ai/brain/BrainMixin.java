package com.github.cao.awa.sepals.mixin.entity.ai.brain;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.entity.ai.brain.TaskDelegate;
import com.github.cao.awa.sepals.entity.ai.task.composite.SepalsTaskStatus;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Supplier;

@Mixin(Brain.class)
public abstract class BrainMixin<E extends LivingEntity> implements TaskDelegate<E> {
    @Shadow
    @Final
    private Map<Integer, Map<Activity, Set<Task<? super E>>>> tasks;

    @Shadow
    @Final
    private Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> memories;

    @Shadow
    public abstract <U> void forget(MemoryModuleType<U> type);

    @Shadow public abstract boolean hasActivity(Activity activity);

    @Unique
    private Catheter<Task<? super E>> taskCatheter;
    @Unique
    private Catheter<Task<? super E>> runningTasks;
    @Unique
    private Catheter<Map.Entry<MemoryModuleType<?>, Optional<? extends Memory<?>>>> memoriesCatheter;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    public void initBrain(
            Collection<? extends MemoryModuleType<?>> memories,
            Collection<? extends SensorType<? extends Sensor<? super E>>> sensors,
            ImmutableList<?> memoryEntries,
            Supplier<Codec<Brain<E>>> codecSupplier,
            CallbackInfo ci
    ) {
        constructTasks();
        constructMemories();
    }

    @Unique
    private void constructTasks() {
        this.taskCatheter = Catheter.of(this.tasks.values())
                .collectionFlatTo(Map::entrySet)
                .filter(this::hasActivity, Map.Entry::getKey)
                .collectionFlatTo(Map.Entry::getValue);
    }

    @Unique
    private Catheter<Task<? super E>> getTasks() {
        if (this.taskCatheter == null) {
            constructTasks();
        }
        return this.taskCatheter;
    }

    @Unique
    private Catheter<Task<? super E>> getRunningTasks() {
        if (this.runningTasks == null) {
            constructRunningTasks();
        }
        return this.runningTasks;
    }

    @Unique
    private void constructRunningTasks() {
        this.runningTasks = getTasks().filterTo(SepalsTaskStatus::isRunning, Task::getStatus);
    }

    @Unique
    private Catheter<Map.Entry<MemoryModuleType<?>, Optional<? extends Memory<?>>>> getMemories() {
        if (this.memoriesCatheter == null) {
            constructMemories();
        }
        return this.memoriesCatheter;
    }

    @Unique
    private void constructMemories() {
        this.memoriesCatheter = Catheter.of(this.memories.entrySet());
    }

    @Inject(
            method = "setMemory",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private <U> void setMemory(MemoryModuleType<U> type, Optional<? extends Memory<?>> memory, CallbackInfo ci) {
        constructMemories();
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/Brain;tickMemories()V"
            )
    )
    private void tickMemories(Brain<E> instance) {
        getMemories().each(entry -> {
            Optional<? extends Memory<?>> memOp = entry.getValue();
            if (memOp.isPresent()) {
                Memory<?> memory = memOp.get();

                if (memory.isExpired()) {
                    forget(entry.getKey());
                }

                memory.tick();
            }
        });
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/Brain;startTasks(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;)V"
            )
    )
    private void startTasks(Brain<E> instance, ServerWorld world, E entity) {
        long time = entity.getEntityWorld().getTime();

        Catheter<Task<? super E>> running = getTasks().filterTo(task -> {
            if (SepalsTaskStatus.isStopped(task.getStatus())) {
                return task.tryStarting(world, entity, time);
            }
            return true;
        });

        if (this.runningTasks != null) {
            this.runningTasks.merge(running);
        } else {
            this.runningTasks = running;
        }
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/Brain;updateTasks(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;)V"
            )
    )
    private void updateTasks(Brain<E> instance, ServerWorld world, E entity) {
        long time = entity.getEntityWorld().getTime();

        getRunningTasks().filter(task -> {
            boolean running = SepalsTaskStatus.isRunning(task.getStatus());
            if (running) {
                task.tick(world, entity, time);
            }
            return running;
        });
    }

    @Inject(
            method = "stopAllTasks",
            at = @At("HEAD"),
            cancellable = true
    )
    private void stopAllTasks(ServerWorld world, E entity, CallbackInfo ci) {
        long time = entity.getEntityWorld().getTime();

        getRunningTasks().each(task -> task.stop(world, entity, time));

        this.runningTasks = null;

        ci.cancel();
    }

    @Inject(
            method = "setTaskList(Lnet/minecraft/entity/ai/brain/Activity;Lcom/google/common/collect/ImmutableList;Ljava/util/Set;Ljava/util/Set;)V",
            at = @At("RETURN")
    )
    private void updateTasks(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<?>>> indexedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories, Set<MemoryModuleType<?>> forgettingMemories, CallbackInfo ci) {
        constructTasks();
    }

    @Inject(
            method = "clear()V",
            at = @At("RETURN")
    )
    private void clearTasks(CallbackInfo ci) {
        this.taskCatheter = null;
        this.runningTasks = null;
    }

    @Override
    public Catheter<Task<? super E>> sepals$tasks() {
        return this.taskCatheter.dump();
    }

    @Inject(
            method = "resetPossibleActivities(Lnet/minecraft/entity/ai/brain/Activity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;add(Ljava/lang/Object;)Z",
                    shift = At.Shift.AFTER
            )
    )
    private void resetPossibleActivities(Activity except, CallbackInfo ci) {
        constructTasks();
    }
}
