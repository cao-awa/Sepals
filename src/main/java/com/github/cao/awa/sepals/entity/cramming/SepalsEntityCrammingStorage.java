package com.github.cao.awa.sepals.entity.cramming;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import net.minecraft.entity.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SepalsEntityCrammingStorage {
    private static final Map<String, List<Entity>> entities = ApricotCollectionFactor.hashMap();
    private static final Map<String, Set<Entity>> getByTypeEntities = ApricotCollectionFactor.hashMap();

    public static Set<Entity> cachedGetByType(String box) {
        return SepalsEntityCrammingStorage.getByTypeEntities.get(box);
    }

    public static List<Entity> cached(String box) {
        return SepalsEntityCrammingStorage.entities.get(box);
    }

    public static void cacheGetByType(String box, Collection<Entity> entities) {
        SepalsEntityCrammingStorage.getByTypeEntities.computeIfAbsent(
                box,
                k -> ApricotCollectionFactor.hashSet()
        ).addAll(entities);
    }

    public static void cache(String box, List<Entity> entities) {
        SepalsEntityCrammingStorage.entities.put(box, entities);
    }

    public static void clear() {
        SepalsEntityCrammingStorage.entities.clear();
        SepalsEntityCrammingStorage.getByTypeEntities.clear();
    }
}
