package com.github.cao.awa.sepals.item;

import net.minecraft.entity.ItemEntity;

import java.util.Collection;
import java.util.List;

public interface BoxedItemEntities {
    default void addEntity(ItemEntity entity) {
        sepals$addEntity(entity);
    }

    default boolean canSetEntities() {
        return sepals$canSetEntities();
    }

    default void setEntities(List<ItemEntity> entities) {
        sepals$setEntities(entities);
    }

    default boolean isEmpty() {
        return sepals$isEmpty();
    }

    default void clearItemBoxed() {
        sepals$clearItemBoxed();
    }

    default void invalidate(ItemEntity entity) {
        sepals$invalidate(entity);
    }

    default Collection<ItemEntity> entities() {
        return sepals$entities();
    }

    default Collection<ItemEntity> entitiesAndInvalidate(ItemEntity entity) {
        return sepals$entitiesAndInvalidate(entity);
    }

    void sepals$addEntity(ItemEntity entity);

    boolean sepals$canSetEntities();

    void sepals$setEntities(List<ItemEntity> entities);

    boolean sepals$isEmpty();

    void sepals$clearItemBoxed();

    void sepals$invalidate(ItemEntity entity);

    Collection<ItemEntity> sepals$entities();

    Collection<ItemEntity> sepals$entitiesAndInvalidate(ItemEntity entity);
}
