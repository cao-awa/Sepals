package com.github.cao.awa.sepals;

import com.github.cao.awa.sepals.config.SepalsConfig;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class Sepals {
    public static final Logger LOGGER = LogManager.getLogger("Sepals");
    public static final String VERSION = "1.0.16";
    public static final SepalsConfig CONFIG = new SepalsConfig();
    public static final SepalsConfig PERSISTENT_CONFIG = new SepalsConfig();
    public static Set<String> LOADED_MODS = CollectionFactor.hashSet();
    public static String loadingPlatform = "fabric";
    public static boolean isLithiumLoaded;
    public static boolean isMoonriseLoaded;
    public static boolean isAsyncLoaded;

    public static void init() {
        LOGGER.info("Sepals '{}' loading on platform '{}'", VERSION, loadingPlatform);
        CONFIG.load();
        PERSISTENT_CONFIG.copyFrom(CONFIG);
        CONFIG.print();
    }

    public static void writeConfig() {
        PERSISTENT_CONFIG.write();
    }
}
