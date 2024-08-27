package com.github.cao.awa.sepals.mixin.world.poi;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.poi.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PointOfInterestStorage.class)
public interface PointOfInterestStorageAccessor {
    @Accessor
    LongSet getPreloadedChunks();
}
