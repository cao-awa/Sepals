package com.github.cao.awa.sepals;

import com.github.cao.awa.sepals.command.SepalsConfigCommand;
import com.github.cao.awa.sepals.command.SepalsDebugCommand;
import com.github.cao.awa.sepals.config.SepalsConfig;
import com.github.cao.awa.sepals.transform.mixin.handler.SepalsMixinHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Sepals implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Sepals");
    public static final String VERSION = "1.0.6";
    public static final SepalsConfig CONFIG = new SepalsConfig();
    public static final SepalsConfig PERSISTENT_CONFIG = new SepalsConfig();
    public static boolean isLithiumLoaded;
    public static boolean isMoonriseLoaded;

    @Override
    public void onInitialize() {
        SepalsMixinHandler.startPostProcess();

        LOGGER.info("Sepals {} loading", VERSION);
        CONFIG.load();
        PERSISTENT_CONFIG.copyFrom(CONFIG);
        CONFIG.print();

        ServerLifecycleEvents.SERVER_STARTING.register(SepalsDebugCommand::register);
        ServerLifecycleEvents.SERVER_STARTING.register(SepalsConfigCommand::register);
    }

    public static void writeConfig() {
        PERSISTENT_CONFIG.write();
    }
}