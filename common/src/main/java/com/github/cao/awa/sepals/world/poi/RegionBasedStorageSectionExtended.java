package com.github.cao.awa.sepals.world.poi;

import com.github.cao.awa.catheter.Catheter;

// Modified from lithium common.
public interface RegionBasedStorageSectionExtended<R> {
    Catheter<R> sepals$getWithinChunkColumn(int x, int z);
}