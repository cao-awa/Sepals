package com.github.cao.awa.sepals.mixin.world.storage;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(SerializingRegionBasedStorage.class)
public interface SerializingRegionBasedStorageAccessor<R> {
    @Accessor
    HeightLimitView getWorld();

    @Invoker
    Optional<R> invokeGet(long pos);
}
