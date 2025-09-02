package com.github.cao.awa.sepals.mixin.server.chunk;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightSourceView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {
    @Inject(
            method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;",
            at = @At("HEAD")
    )
    public void getChunk(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {

    }
}
