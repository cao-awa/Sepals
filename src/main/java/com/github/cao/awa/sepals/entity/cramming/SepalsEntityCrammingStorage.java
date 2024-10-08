package com.github.cao.awa.sepals.entity.cramming;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Map;

public class SepalsEntityCrammingStorage {
    private static final Map<String, List<Entity>> entities = ApricotCollectionFactor.hashMap();

    public static List<Entity> cached(String box) {
        return SepalsEntityCrammingStorage.entities.get(box);
    }

    public static void cache(String box, List<Entity> entities) {
        SepalsEntityCrammingStorage.entities.put(box, entities);
    }

    public static void clear() {
        SepalsEntityCrammingStorage.entities.clear();
    }
}
