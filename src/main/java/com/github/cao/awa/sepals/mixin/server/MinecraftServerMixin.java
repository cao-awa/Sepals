package com.github.cao.awa.sepals.mixin.server;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.entity.cramming.SepalsEntityCrammingStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    public void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        SepalsEntityCrammingStorage.clear();
    }
}
