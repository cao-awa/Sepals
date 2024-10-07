package com.github.cao.awa.sepals;

import com.github.cao.awa.sepals.command.SepalsConfigCommand;
import com.github.cao.awa.sepals.command.SepalsDebugCommand;
import com.github.cao.awa.sepals.transform.mixin.handler.SepalsMixinHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Sepals implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Sepals");
    public static final String VERSION = "1.0.1";
    public static boolean forceEnableSepalsPoi = true;
    public static boolean enableSepalsVillager = true;
    public static boolean enableSepalsFrogLookAt = true;
    public static boolean enableSepalsFrogAttackableSensor = false;
    public static boolean enableSepalsLivingTargetCache = true;
    public static boolean nearestLivingEntitiesSensorUseQuickSort = true;
    public static boolean enableSepalsBiasedJumpLongTask = true;
    public static boolean enableSepalsWeightTable = false;
    public static boolean enableEntitiesCramming = false;

    @Override
    public void onInitializeServer() {
        SepalsMixinHandler.startPostProcess();

        LOGGER.info("Sepals {} loading", VERSION);
        LOGGER.info("Sepals 'enableSepalsVillager' flag is {}, this flag unable to change in game runtime!", enableSepalsVillager);
        LOGGER.info("Sepals 'enableSepalsFrogLookAt' flag is {}, this flag unable to change in game runtime!", enableSepalsFrogLookAt);
        LOGGER.info("Sepals 'enableSepalsFrogAttackableSensor' flag is {}", enableSepalsFrogAttackableSensor);
        LOGGER.info("Sepals 'enableSepalsLivingTargetCache' flag is {}", enableSepalsLivingTargetCache);
        LOGGER.info("Sepals 'nearestLivingEntitiesSensorUseQuickSort' flag is {}", nearestLivingEntitiesSensorUseQuickSort);
        LOGGER.info("Sepals 'enableSepalsBiasedJumpLongTask' flag is {}, this flag unable to change in game runtime!", enableSepalsBiasedJumpLongTask);
        LOGGER.info("Sepals 'enableSepalsWeightTable' flag is {}", enableSepalsWeightTable);
        LOGGER.info("Sepals 'enableEntitiesCramming' flag is {}", enableEntitiesCramming);

        ServerLifecycleEvents.SERVER_STARTING.register(SepalsDebugCommand::register);
        ServerLifecycleEvents.SERVER_STARTING.register(SepalsConfigCommand::register);
    }
}