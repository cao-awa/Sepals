package com.github.cao.awa.sepals.mixin.world.storage;

import com.github.cao.awa.sepals.world.poi.SepalsPointOfInterestStorage;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(PointOfInterestSet.class)
public class PointOfInterestSetMixin {
    @Unique
    private PointOfInterestSet instance() {
        return (PointOfInterestSet) (Object) this;
    }

    @Inject(
            method = "get(Ljava/util/function/Predicate;Lnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;)Ljava/util/stream/Stream;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void get(
            Predicate<RegistryEntry<PointOfInterestType>> predicate,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            CallbackInfoReturnable<Stream<PointOfInterest>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.get(
                instance(),
                predicate,
                occupationStatus
        ).stream());
    }
}
