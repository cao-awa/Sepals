package com.github.cao.awa.sepals.mixin.world.poi;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.world.poi.RegionBasedStorageSectionExtended;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.util.collections.ListeningLong2ObjectOpenHashMap;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ChunkErrorHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.storage.ChunkPosKeyedStorage;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Function;

// Modified from lithium common.
@Mixin(SerializingRegionBasedStorage.class)
public abstract class SerializingRegionBasedStorageMixin<R> implements RegionBasedStorageSectionExtended<R> {
    @Mutable
    @Shadow
    @Final
    private Long2ObjectMap<Optional<R>> loadedElements;

    @Shadow
    protected abstract Optional<R> get(long pos);

    @Shadow
    protected abstract void loadDataAt(ChunkPos pos);

    @Shadow
    @Final
    protected HeightLimitView world;

    @Unique
    private Map<Long, BitSet> columns;

    @SuppressWarnings("rawtypes")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(
            ChunkPosKeyedStorage storageAccess, Function codecFactory, Function factory, DynamicRegistryManager registryManager, ChunkErrorHandler errorHandler, HeightLimitView world, CallbackInfo ci
    ) {
        this.columns = new Long2ObjectOpenHashMap<>();
        this.loadedElements = new ListeningLong2ObjectOpenHashMap<>(this::onEntryAdded, this::onEntryRemoved);
    }

    @Unique
    private void onEntryRemoved(long key, Optional<R> value) {
        int y = ChunkSectionPos.unpackY(key) - this.world.getBottomSectionCoord();

        // We only care about items belonging to a valid sub-chunk
        if (y > -1 && y < this.world.countVerticalSections()) {
            int x = ChunkSectionPos.unpackX(key);
            int z = ChunkSectionPos.unpackZ(key);

            long pos = ChunkPos.toLong(x, z);
            BitSet flags = this.columns.get(pos);

            if (flags != null) {
                flags.clear(y);
                if (flags.isEmpty()) {
                    this.columns.remove(pos);
                }
            }
        }
    }

    @Unique
    private void onEntryAdded(long key, Optional<R> value) {
        int y = ChunkSectionPos.unpackY(key) - this.world.getBottomSectionCoord();

        // We only care about items belonging to a valid sub-chunk
        if (y > -1 && y < this.world.countVerticalSections()) {
            int x = ChunkSectionPos.unpackX(key);
            int z = ChunkSectionPos.unpackZ(key);

            BitSet flags = this.columns.computeIfAbsent(ChunkPos.toLong(x, z), k -> new BitSet());

            flags.set(y, value.isPresent());
        }
    }

    @Override
    public Catheter<R> sepals$getWithinChunkColumn(int x, int z) {
        BitSet sectionsWithPOI = getNonEmptyPOISections(x, z);

        if (sectionsWithPOI.isEmpty()) {
            return Catheter.make();
        }

        List<R> list = ApricotCollectionFactor.arrayList();
        int minYSection = this.world.getBottomSectionCoord();
        int chunkYIndex = sectionsWithPOI.nextSetBit(0);
        while (chunkYIndex != -1) {
            int chunkY = chunkYIndex + minYSection;
            //noinspection SimplifyOptionalCallChains
            R r = this.loadedElements.get(ChunkSectionPos.asLong(x, chunkY, z)).orElse(null);
            if (r != null) {
                list.add(r);
            }
            chunkYIndex = sectionsWithPOI.nextSetBit(chunkYIndex + 1);
        }

        return Catheter.of(list);
    }

    @Unique
    private BitSet getNonEmptyPOISections(int chunkX, int chunkZ) {
        long pos = ChunkPos.toLong(chunkX, chunkZ);

        BitSet flags = getNonEmptySections(pos, false);

        if (flags == null) {
            loadDataAt(new ChunkPos(pos));

            return getNonEmptySections(pos, true);
        } else {
            return flags;
        }

    }

    @Unique
    private BitSet getNonEmptySections(long pos, boolean required) {
        BitSet set = this.columns.get(pos);

        if (set == null && required) {
            throw new NullPointerException("No data is present for column: " + new ChunkPos(pos));
        }

        return set;
    }
}
