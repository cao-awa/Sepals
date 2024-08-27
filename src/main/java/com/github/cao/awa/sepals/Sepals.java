package com.github.cao.awa.sepals;

import com.github.cao.awa.sepals.backup.SepalsBackup;
import com.github.cao.awa.sepals.backup.SepalsBackupCenter;
import com.github.cao.awa.sepals.command.SepalsBackupCommand;
import com.github.cao.awa.sepals.command.SepalsConfigCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class Sepals implements ModInitializer {
    public static SepalsBackupCenter backupCenter;
    public static boolean enableSepalsFrogLookAt= false;
    public static boolean enableSepalsFrogAttackableSensor = false;
    public static boolean enableSepalsLivingTargetCache = false;
    public static boolean nearestLivingEntitiesSensorUseQuickSort = false;
    public static boolean enableSepalsBiasedJumpLongTask = false;
    public static boolean enableSepalsWeightTable = false;
    public static boolean enableEntitiesCramming = false;

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(SepalsConfigCommand::register);
        ServerLifecycleEvents.SERVER_STARTING.register(SepalsBackupCommand::register);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                backupCenter = SepalsBackupCenter.fromServer(server);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

// /spark profiler start --timeout 60