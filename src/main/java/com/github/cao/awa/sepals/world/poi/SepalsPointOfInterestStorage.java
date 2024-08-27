package com.github.cao.awa.sepals.world.poi;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.catheter.IntCatheter;
import com.github.cao.awa.catheter.receptacle.Receptacle;
import com.github.cao.awa.sepals.mixin.world.poi.PointOfInterestAccessor;
import com.github.cao.awa.sepals.mixin.world.poi.PointOfInterestSetAccessor;
import com.github.cao.awa.sepals.mixin.world.poi.PointOfInterestStorageAccessor;
import com.github.cao.awa.sepals.mixin.world.storage.SerializingRegionBasedStorageAccessor;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ChunkErrorHandler;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.SectionDistanceLevelPropagator;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.storage.ChunkPosKeyedStorage;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import net.minecraft.world.storage.StorageKey;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SepalsPointOfInterestStorage {
    public static Catheter<PointOfInterest> getInSquare(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        int i = Math.floorDiv(radius, 16) + 1;
        return Catheter.of(ChunkPos.stream(new ChunkPos(pos), i).toList())
                .flatTo(chunkPos -> getInChunk(storage, typePredicate, chunkPos, occupationStatus))
                .filter(poi -> {
                    BlockPos blockPos2 = poi.getPos();
                    return Math.abs(blockPos2.getX() - pos.getX()) <= radius && Math.abs(blockPos2.getZ() - pos.getZ()) <= radius;
                });
    }

    @Debug
    public static Catheter<PointOfInterest> getInChunk(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate, ChunkPos chunkPos, PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        SerializingRegionBasedStorageAccessor<PointOfInterestSet> storageInstance = storageAccessor(storage);
        return Catheter.of(
                        IntStream.range(
                                        storageInstance.getWorld().getBottomSectionCoord(),
                                        storageInstance.getWorld().getTopSectionCoord()
                                )
                                .boxed()
                                .toArray(Integer[]::new)
                )
                .vary(integer -> storageInstance.invokeGet(ChunkSectionPos.from(chunkPos, integer).asLong()))
                .filter(Optional::isPresent)
                .flatTo(optional -> get(optional.get(), typePredicate, occupationStatus));
    }

    @SuppressWarnings("unchecked")
    public static Catheter<PointOfInterest> get(
            PointOfInterestSet set,
            Predicate<RegistryEntry<PointOfInterestType>> predicate,
            PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        return Catheter.of(accessor(set).getPointsOfInterestByType().entrySet())
                .filter(entry -> predicate.test(entry.getKey()))
                .flatTo(entry -> Catheter.of(entry.getValue()))
                .filter((Predicate<PointOfInterest>) occupationStatus.getPredicate());
    }

    public static Catheter<PointOfInterest> getInCircle(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        int i = radius * radius;
        return getInSquare(storage, typePredicate, pos, radius, occupationStatus)
                .filter(poi -> poi.getPos().getSquaredDistance(pos) <= (double) i);
    }

    public static Catheter<BlockPos> getPositions(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        return getInCircle(storage, typePredicate, pos, radius, occupationStatus).vary(PointOfInterest::getPos).filter(posPredicate);
    }

    public static Catheter<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> getTypesAndPositions(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        return getInCircle(storage, typePredicate, pos, radius, occupationStatus)
                .filter(poi -> posPredicate.test(poi.getPos()))
                .vary(poi -> Pair.of(poi.getType(), poi.getPos()))
                .arrayGenerator(Pair[]::new);
    }

    public static Catheter<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> getSortedTypesAndPositions(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        return getTypesAndPositions(storage, typePredicate, posPredicate, pos, radius, occupationStatus)
                .sort(Comparator.comparingDouble(pair -> pair.getSecond().getSquaredDistance(pos)));
    }

    public Optional<BlockPos> getPosition(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        return Optional.ofNullable(
                getPositions(storage, typePredicate, posPredicate, pos, radius, occupationStatus)
                        .findFirst(x -> true)
        );
    }

    public Optional<BlockPos> getNearestPosition(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            BlockPos pos,
            int radius, PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        return Optional.ofNullable(
                getInCircle(storage, typePredicate, pos, radius, occupationStatus)
                        .vary(PointOfInterest::getPos)
                        .min(Comparator.comparingDouble(blockPos2 -> blockPos2.getSquaredDistance(pos)))
        );
    }

    public Optional<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> getNearestTypeAndPosition(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            BlockPos pos, int radius, PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        return Optional.ofNullable(
                        getInCircle(storage, typePredicate, pos, radius, occupationStatus)
                                .min(Comparator.comparingDouble(poi -> poi.getPos().getSquaredDistance(pos)))
                )
                .map(poi -> Pair.of(poi.getType(), poi.getPos()));
    }

    public Optional<BlockPos> getNearestPosition(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus
    ) {
        return Optional.ofNullable(
                getInCircle(storage, typePredicate, pos, radius, occupationStatus)
                        .vary(PointOfInterest::getPos)
                        .filter(posPredicate)
                        .min(Comparator.comparingDouble(blockPos2 -> blockPos2.getSquaredDistance(pos)))
        );
    }

    public static Optional<BlockPos> getPosition(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            BiPredicate<RegistryEntry<PointOfInterestType>, BlockPos> biPredicate,
            BlockPos pos,
            int radius
    ) {
        return Optional.ofNullable(
                getInCircle(storage, typePredicate, pos, radius, PointOfInterestStorage.OccupationStatus.HAS_SPACE)
                        .filter(poi -> biPredicate.test(poi.getType(), poi.getPos()))
                        .findFirst(x -> true)
        ).map(poi -> {
            ((PointOfInterestAccessor) poi).invokeReserveTicket();
            return poi.getPos();
        });
    }

    public Optional<BlockPos> getPosition(
            PointOfInterestStorage storage,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> positionPredicate,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            BlockPos pos,
            int radius,
            Random random
    ) {
        Catheter<PointOfInterest> catheter = getInCircle(storage, typePredicate, pos, radius, occupationStatus);
        shuffle(catheter.dArray(), random);
        return Optional.ofNullable(
                catheter.filter(poi -> positionPredicate.test(poi.getPos()))
                        .findFirst(x -> true)
        ).map(PointOfInterest::getPos);
    }

    public static <T> void shuffle(T[] elements, Random random) {
        int i = elements.length;

        for (int j = i; j > 1; --j) {
            int swapTo = random.nextInt(j);
            int swapFrom = j - 1;
            T fromElement = elements[swapFrom];
            T toElement = elements[swapTo];
            elements[swapTo] = fromElement;
            elements[swapFrom] = toElement;
        }
    }

    /**
     * Preloads chunks in a square area with the given radius. Loads the chunks with {@code ChunkStatus.EMPTY}.
     *
     * @param radius the radius in blocks
     */
    public static void preloadChunks(PointOfInterestStorage storage, WorldView world, BlockPos pos, int radius) {
        Catheter.of(ChunkSectionPos.stream(
                                new ChunkPos(pos),
                                Math.floorDiv(radius, 16),
                                storageAccessor(storage).getWorld().getBottomSectionCoord(),
                                storageAccessor(storage).getWorld().getTopSectionCoord()
                        ).toList()
                )
                .vary(sectionPos -> Pair.of(sectionPos, storageAccessor(storage).invokeGet(sectionPos.asLong())))
                .discard(pair -> pair.getSecond().map(SepalsPointOfInterestStorage::isValid).orElse(false))
                .vary(pair -> pair.getFirst().toChunkPos())
                .filter(chunkPos -> accessor(storage).getPreloadedChunks().add(chunkPos.toLong()))
                .each(chunkPos -> world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.EMPTY));
    }

    private static PointOfInterestStorageAccessor accessor(PointOfInterestStorage storage) {
        return (PointOfInterestStorageAccessor) storage;
    }

    private static PointOfInterestSetAccessor accessor(PointOfInterestSet set) {
        return (PointOfInterestSetAccessor) set;
    }

    private static boolean isValid(PointOfInterestSet set) {
        return accessor(set).invokeIsValid();
    }

    @SuppressWarnings("unchecked")
    private static SerializingRegionBasedStorageAccessor<PointOfInterestSet> storageAccessor(PointOfInterestStorage storage) {
        return (SerializingRegionBasedStorageAccessor<PointOfInterestSet>) storage;
    }
}
