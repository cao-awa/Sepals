package com.github.cao.awa.sepals.entity.intersects;

import com.github.cao.awa.sepals.mixin.world.WorldAccessor;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.math.Box;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SepalsWorldEntityIntersects {
    private int crammingCount = 0;

    public void quickInterestOtherEntities(World world, @Nullable Entity except, Box box, Predicate<? super Entity> predicate, Consumer<Entity> action) {
        this.crammingCount = 0;

        List<Entity> entities = CollectionFactor.arrayList();

        ((WorldAccessor) world).invokeGetEntityLookup().forEachIntersects(box, (entity) -> {
            if (entity == except) {
                return;
            }

            entities.add(entity);
        });
        for (Entity entity : entities) {
            if (predicate.test(entity)) {
                tryDoEntityInterestWithoutCramming(entity, action);
            }
        }

        for (EnderDragonPart enderDragonPart : world.getEnderDragonParts()) {
            if (enderDragonPart != except && enderDragonPart.owner != except && predicate.test(enderDragonPart) && box.intersects(enderDragonPart.getBoundingBox())) {
                tryDoEntityInterestWithoutCramming(enderDragonPart, action);
            }
        }
    }

    public void quickInterestOtherEntities(World world, @Nullable Entity except, Box box, Predicate<? super Entity> predicate, Consumer<Entity> action, Consumer<Entity> crammingAction, int crammingLimit) {
        this.crammingCount = 0;

        List<Entity> entities = CollectionFactor.arrayList();

        ((WorldAccessor) world).invokeGetEntityLookup().forEachIntersects(box, (entity) -> {
            if (entity == except) {
                return;
            }

            entities.add(entity);
        });

        for (Entity entity : entities) {
            if (predicate.test(entity)) {
                tryDoEntityInterest(entity, action, crammingAction, crammingLimit);
            }
        }

        for (EnderDragonPart enderDragonPart : world.getEnderDragonParts()) {
            if (enderDragonPart == except || enderDragonPart.owner == except) {
                continue;
            }
            if (predicate.test(enderDragonPart) && box.intersects(enderDragonPart.getBoundingBox())) {
                tryDoEntityInterest(enderDragonPart, action, crammingAction, crammingLimit);
            }
        }
    }

    private void tryDoEntityInterestWithoutCramming(@NotNull final Entity entity, final Consumer<Entity> action) {
        tryDoEntityActionInterest(entity, action);
    }

    private void tryDoEntityInterest(@NotNull final Entity entity, final Consumer<Entity> action, final Consumer<Entity> crammingAction, final int crammingLimit) {
        if (!entity.hasVehicle()) {
            ++this.crammingCount;
        }
        tryDoEntityActionInterest(entity, action);
        tryEntityCrammingInterest(entity, crammingAction, crammingLimit);
    }

    private void tryDoEntityActionInterest(@NotNull final Entity entity, Consumer<Entity> action) {
        action.accept(entity);
    }

    private void tryEntityCrammingInterest(@NotNull final Entity entity, final Consumer<Entity> action, final int crammingLimit) {
        if (this.crammingCount > crammingLimit) {
            action.accept(entity);
        }
    }
}
