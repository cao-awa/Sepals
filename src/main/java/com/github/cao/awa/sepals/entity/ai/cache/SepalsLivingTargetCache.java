package com.github.cao.awa.sepals.entity.ai.cache;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SepalsLivingTargetCache extends LivingTargetCache {
    private final LivingEntity[] entities;
    private final Object2BooleanOpenHashMap<LivingEntity> directSuccess;
    private final Predicate<LivingEntity> compute;

    @SuppressWarnings("unchecked")
    public SepalsLivingTargetCache(LivingEntity owner, List<LivingEntity> entities) {
        super(owner, Collections.EMPTY_LIST);
        this.entities = entities.toArray(LivingEntity[]::new);
        this.directSuccess = new Object2BooleanOpenHashMap<>(entities.size());
        Predicate<LivingEntity> predicate = entity -> Sensor.testTargetPredicate(owner, entity);
        this.compute = entity -> this.directSuccess.computeIfAbsent(entity, predicate);
    }

    @SuppressWarnings("unchecked")
    public SepalsLivingTargetCache(LivingEntity owner, LivingEntity[] entities) {
        super(owner, Collections.EMPTY_LIST);
        this.entities = entities;
        this.directSuccess = new Object2BooleanOpenHashMap<>(entities.length);
        Predicate<LivingEntity> predicate = entity -> Sensor.testTargetPredicate(owner, entity);
        this.compute = entity -> this.directSuccess.computeIfAbsent(entity, predicate);
    }

    public Optional<LivingEntity> findFirst(Predicate<LivingEntity> predicate) {
        LivingEntity[] entities = this.entities;

        int i = 0;
        int edge = entities.length;
        for (; i < edge; i++) {
            LivingEntity entity = entities[i];
            if (predicate.test(entity) && this.compute.test(entity)) {
                return Optional.of(entity);
            }
        }

        return Optional.empty();
    }

    public Optional<LivingEntity> findFirstPlayer(Predicate<LivingEntity> predicate) {
        LivingEntity[] entities = this.entities;

        int i = 0;
        int edge = entities.length;
        for (; i < edge; i++) {
            LivingEntity entity = entities[i];
            if (entity.isPlayer() && predicate.test(entity) && this.compute.test(entity)) {
                return Optional.of(entity);
            }
        }

        return Optional.empty();
    }

    public Optional<LivingEntity> findFirstPlayer(Predicate<LivingEntity> predicate, Supplier<Boolean> canTargetPredicateWhen) {
        LivingEntity[] entities = this.entities;

        int i = 0;
        int edge = entities.length;
        for (; i < edge; i++) {
            LivingEntity entity = entities[i];
            if (entity.isPlayer() && predicate.test(entity) && canTargetPredicateWhen.get() && this.compute.test(entity)) {
                return Optional.of(entity);
            }
        }

        return Optional.empty();
    }

    public Iterable<LivingEntity> iterate(Predicate<LivingEntity> predicate) {
        return Iterables.filter(Arrays.asList(this.entities), entity -> predicate.test(entity) && this.compute.test(entity));
    }

    public Stream<LivingEntity> stream(Predicate<LivingEntity> predicate) {
        return Arrays.stream(this.entities).filter(entity -> predicate.test(entity) && this.compute.test(entity));
    }

    public boolean contains(LivingEntity target) {
        int i = 0;
        int edge = entities.length;
        for (; i < edge; i++) {
            LivingEntity entity = entities[i];
            if (entity == target) {
                return this.compute.test(entity);
            }
        }

        return false;
    }

    public boolean anyMatch(Predicate<LivingEntity> predicate) {
        LivingEntity[] entities = this.entities;

        int i = 0;
        int edge = entities.length;
        for (; i < edge; i++) {
            LivingEntity entity = entities[i];
            if (predicate.test(entity) && this.compute.test(entity)) {
                return true;
            }
        }

        return false;
    }
}
