package com.github.cao.awa.sepals.world.poi;

import com.github.cao.awa.catheter.Catheter;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class SepalsPointOfInterestSet {
    public static Catheter<PointOfInterest> get(Map<RegistryEntry<PointOfInterestType>, Set<PointOfInterest>> pointsOfInterestByType, Predicate<RegistryEntry<PointOfInterestType>> predicate, PointOfInterestStorage.OccupationStatus occupationStatus) {
            return Catheter.combineSet(Catheter.of(
                    pointsOfInterestByType.entrySet()
            ).filter(
                    (entry) -> predicate.test(entry.getKey())
            ).varyTo(
                    Map.Entry::getValue
            )).filter(
                    occupationStatus.getPredicate()
            );
    }
}
