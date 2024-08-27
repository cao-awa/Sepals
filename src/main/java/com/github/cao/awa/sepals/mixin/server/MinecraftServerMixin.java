package com.github.cao.awa.sepals.mixin.server;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.backup.SepalsBackup;
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

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow @Final protected LevelStorage.Session session;

    @Shadow private PlayerManager playerManager;

    @Inject(
            method = "shutdown",
            at = @At("HEAD"),
            cancellable = true
    )
    public void shutdownRollbackBackup(CallbackInfo ci) throws IOException {
        if (Sepals.backupCenter.wantRollback() != -1) {
            this.playerManager.disconnectAllPlayers();

            Sepals.backupCenter.doRollback((MinecraftServer) (Object) this, null);

            this.session.close();

            ci.cancel();
        }
    }

    @Inject(
            method = "save",
            at = @At("HEAD"),
            cancellable = true
    )
    public void cancelSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (Sepals.backupCenter.wantRollback() != -1 || Sepals.backupCenter.backupInProgress()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "saveAll",
            at = @At("HEAD"),
            cancellable = true
    )
    public void cancelSaveAll(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (Sepals.backupCenter.wantRollback() != -1 || Sepals.backupCenter.backupInProgress()) {
            cir.setReturnValue(false);
        }
    }
}
