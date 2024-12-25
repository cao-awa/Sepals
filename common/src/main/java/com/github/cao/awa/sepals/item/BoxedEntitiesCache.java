package com.github.cao.awa.sepals.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;

import java.util.Collection;
import java.util.List;

public interface BoxedEntitiesCache {
    default void cache(Box box, List<Entity> entities) {
        sepals$cache(box, entities);
    }

    default void clearCache() {
        sepals$clearCache();
    }

    default List<Entity> cached(Box box) {
        return sepals$cached(box);
    }

    void sepals$cache(Box box, List<Entity> entities);

    void sepals$clearCache();

    List<Entity> sepals$cached(Box box);
}
