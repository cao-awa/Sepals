package com.github.cao.awa.sepals.world.poi;

import com.github.cao.awa.catheter.Catheter;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface RegionBasedStorageSectionExtended<R> {
    Catheter<R> sepals$getWithinChunkColumn(int x, int z);
    Stream<PointOfInterest> sepals$getInChunk(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, ChunkPos chunkPos, PointOfInterestStorage.OccupationStatus occupationStatus);
}