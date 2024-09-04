package com.github.cao.awa.sepals.mixin.world.storage;

import com.github.cao.awa.sepals.world.poi.SepalsPointOfInterestStorage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(PointOfInterestStorage.class)
public class PointOfInterestStorageMixin {
    @Unique
    private PointOfInterestStorage instance() {
        return (PointOfInterestStorage) (Object) this;
    }

    @Inject(
            method = "getInSquare",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getInSquare(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, PointOfInterestStorage.OccupationStatus occupationStatus, CallbackInfoReturnable<Stream<PointOfInterest>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getInSquare(
                instance(),
                typePredicate,
                pos,
                radius,
                occupationStatus
        ).stream());
    }

    @Debug
    @Inject(
            method = "getInChunk",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getInChunk(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            ChunkPos chunkPos,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            CallbackInfoReturnable<Stream<PointOfInterest>> cir
    ) {
        if (!SepalsPointOfInterestStorage.isLithiumLoaded()) {
            cir.setReturnValue(
                    SepalsPointOfInterestStorage.getInChunk(
                            instance(),
                            typePredicate,
                            chunkPos,
                            occupationStatus
                    ).stream());
        }
    }

    @Inject(
            method = "getInCircle",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getInCircle(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            CallbackInfoReturnable<Stream<PointOfInterest>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getInCircle(
                instance(),
                typePredicate,
                pos,
                radius,
                occupationStatus
        ).stream());
    }

    @Inject(
            method = "getPositions",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getPositions(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            CallbackInfoReturnable<Stream<BlockPos>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getPositions(
                instance(),
                typePredicate,
                posPredicate,
                pos,
                radius,
                occupationStatus
        ).stream());
    }

    @Inject(
            method = "getTypesAndPositions",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getTypesAndPositions(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            CallbackInfoReturnable<Stream<Pair<RegistryEntry<PointOfInterestType>, BlockPos>>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getTypesAndPositions(
                instance(),
                typePredicate,
                posPredicate,
                pos,
                radius,
                occupationStatus
        ).stream());
    }

    @Inject(
            method = "getTypesAndPositions",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getSortedTypesAndPositions(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            CallbackInfoReturnable<Stream<Pair<RegistryEntry<PointOfInterestType>, BlockPos>>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getSortedTypesAndPositions(
                instance(),
                typePredicate,
                posPredicate,
                pos,
                radius,
                occupationStatus
        ).stream());
    }

    @Inject(
            method = "getPosition(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getPosition(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            CallbackInfoReturnable<Optional<BlockPos>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getPosition(
                instance(),
                typePredicate,
                posPredicate,
                pos,
                radius,
                occupationStatus
        ));
    }

    @Inject(
            method = "getNearestPosition(Ljava/util/function/Predicate;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getNearestPosition(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            CallbackInfoReturnable<Optional<BlockPos>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getNearestPosition(
                instance(),
                typePredicate,
                pos,
                radius,
                occupationStatus
        ));
    }

    @Inject(
            method = "getNearestTypeAndPosition",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getNearestTypeAndPosition(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            CallbackInfoReturnable<Optional<Pair<RegistryEntry<PointOfInterestType>, BlockPos>>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getNearestTypeAndPosition(
                instance(),
                typePredicate,
                pos,
                radius,
                occupationStatus
        ));
    }

    @Inject(
            method = "getNearestPosition(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getNearestPosition(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            BlockPos pos,
            int radius,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            CallbackInfoReturnable<Optional<BlockPos>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getNearestPosition(
                instance(),
                typePredicate,
                posPredicate,
                pos,
                radius,
                occupationStatus
        ));
    }

    @Inject(
            method = "getPosition(Ljava/util/function/Predicate;Ljava/util/function/BiPredicate;Lnet/minecraft/util/math/BlockPos;I)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getPosition(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            BiPredicate<RegistryEntry<PointOfInterestType>, BlockPos> biPredicate,
            BlockPos pos,
            int radius,
            CallbackInfoReturnable<Optional<BlockPos>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getPosition(
                instance(),
                typePredicate,
                biPredicate,
                pos,
                radius
        ));
    }

    @Inject(
            method = "getPosition(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/util/math/random/Random;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getPosition(
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            Predicate<BlockPos> posPredicate,
            PointOfInterestStorage.OccupationStatus occupationStatus,
            BlockPos pos,
            int radius,
            Random random,
            CallbackInfoReturnable<Optional<BlockPos>> cir
    ) {
        cir.setReturnValue(SepalsPointOfInterestStorage.getPosition(
                instance(),
                typePredicate,
                posPredicate,
                occupationStatus,
                pos,
                radius,
                random
        ));
    }


    @Inject(
            method = "preloadChunks",
            at = @At("HEAD"),
            cancellable = true
    )
    public void preloadChunks(WorldView world, BlockPos pos, int radius, CallbackInfo ci) {
        SepalsPointOfInterestStorage.preloadChunks(
                instance(),
                world,
                pos,
                radius
        );

        ci.cancel();
    }
}
