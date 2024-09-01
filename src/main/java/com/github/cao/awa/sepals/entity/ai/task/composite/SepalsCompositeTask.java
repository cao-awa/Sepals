package com.github.cao.awa.sepals.entity.ai.task.composite;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.entity.ai.brain.DetailedDebuggableTask;
import com.github.cao.awa.sepals.entity.ai.brain.TaskDelegate;
import com.github.cao.awa.sepals.weight.WeightedList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SepalsCompositeTask<E extends LivingEntity> implements Task<E>, TaskDelegate<E>, DetailedDebuggableTask {
    private final Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState;
    private final Set<MemoryModuleType<?>> memoriesToForgetWhenStopped;
    protected final Order order;
    protected final RunMode runMode;
    protected final WeightedList<Task<? super E>> tasks = new WeightedList<>();
    protected MultiTickTask.Status status = MultiTickTask.Status.STOPPED;

    public SepalsCompositeTask(
            Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState,
            Set<MemoryModuleType<?>> memoriesToForgetWhenStopped,
            Order order,
            RunMode runMode,
            List<Pair<? extends Task<? super E>, Integer>> tasks
    ) {
        this.requiredMemoryState = requiredMemoryState;
        this.memoriesToForgetWhenStopped = memoriesToForgetWhenStopped;
        this.order = order;
        this.runMode = runMode;
        tasks.forEach(task -> this.tasks.add(task.getFirst(), task.getSecond()));
    }

    @Override
    public MultiTickTask.Status getStatus() {
        return this.status;
    }

    boolean shouldStart(E entity) {
        for(Map.Entry<MemoryModuleType<?>, MemoryModuleState> entry : this.requiredMemoryState.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            MemoryModuleState memoryModuleState = entry.getValue();
            if (!entity.getBrain().isMemoryInState(memoryModuleType, memoryModuleState)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean tryStarting(ServerWorld world, E entity, long time) {
        if (this.shouldStart(entity)) {
            this.status = MultiTickTask.Status.RUNNING;
            this.order.apply(this.tasks);
            this.runMode.run(this.tasks.elements(), world, entity, time);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final void tick(ServerWorld world, E entity, long time) {
        this.tasks.elements()
                .filter(SepalsTaskStatus::isRunning)
                .ifEmpty(x -> stop(world, entity, time))
                .each(task -> task.tick(world, entity, time));
    }

    @Override
    public final void stop(ServerWorld world, E entity, long time) {
        this.status = MultiTickTask.Status.STOPPED;
        this.tasks.elements().filter(SepalsTaskStatus::isRunning).each(task -> task.stop(world, entity, time));
        this.memoriesToForgetWhenStopped.forEach(entity.getBrain()::forget);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public String toString() {
        Set<? extends Task<? super E>> set = this.tasks.elements().filter(SepalsTaskStatus::isRunning).set();
        return "(" + getName() + "): " + set;
    }

    @Override
    public Catheter<Task<? super E>> sepals$tasks() {
        return this.tasks.elements();
    }

    @Override
    public String information() {
        String orderMode = this.order == Order.SHUFFLED ? "SHUFFLED" : "ORDERED";
        String runMode = this.runMode == RunMode.RUN_ONE ? "PICK ONCE" : "RUN ALL";

        return "CompositeTask(" + this.status + ", " + orderMode + ", " + runMode + ", tasks(count=" + this.tasks.size() + "))";
    }

    public enum Order {
        ORDERED(list -> {
        }),
        SHUFFLED(WeightedList::shuffle);

        private final Consumer<WeightedList<?>> listModifier;

        Order(final Consumer<WeightedList<?>> listModifier) {
            this.listModifier = listModifier;
        }

        public void apply(WeightedList<?> list) {
            this.listModifier.accept(list);
        }
    }

    public enum RunMode {
        RUN_ONE {
            @Override
            public <E extends LivingEntity> void run(Catheter<Task<? super E>> tasks, ServerWorld world, E entity, long time) {
                tasks.filter(SepalsTaskStatus::isStopped)
                        .till(task -> task.tryStarting(world, entity, time));
            }
        },
        TRY_ALL {
            @Override
            public <E extends LivingEntity> void run(Catheter<Task<? super E>> tasks, ServerWorld world, E entity, long time) {
                tasks.filter(SepalsTaskStatus::isStopped)
                        .each(task -> task.tryStarting(world, entity, time));
            }
        };

        public abstract <E extends LivingEntity> void run(Catheter<Task<? super E>> tasks, ServerWorld world, E entity, long time);
    }
}
