package com.github.cao.awa.sepals.mixin.server;

import com.github.cao.awa.sepals.item.BoxedEntitiesCache;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow @Final private Map<RegistryKey<World>, ServerWorld> worlds;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    public void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        for (ServerWorld world : this.worlds.values()) {
            if (world instanceof BoxedEntitiesCache entities) {
                entities.clearCache();
            }
        }
    }
}
