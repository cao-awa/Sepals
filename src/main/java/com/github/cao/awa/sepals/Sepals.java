package com.github.cao.awa.sepals;

import com.github.cao.awa.sepals.command.SepalsConfigCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class Sepals implements ModInitializer {
    public static boolean nearestLivingEntitiesSensorUseQuickSort = true;
    public static boolean enableSepalsBiasedJumpLongTask = true;
    public static boolean enableSepalsWeightTable = true;
    public static boolean enableEntitiesCramming = true;

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(SepalsConfigCommand::register);
    }
}

// /spark profiler start --timeout 60