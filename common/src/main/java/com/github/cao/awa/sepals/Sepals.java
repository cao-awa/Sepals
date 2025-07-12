package com.github.cao.awa.sepals;

import com.github.cao.awa.sepals.config.SepalsConfig;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class Sepals {
    public static final Logger LOGGER = LogManager.getLogger("Sepals");
    public static final String VERSION = "1.0.22-FIX2";
    public static final SepalsConfig CONFIG = new SepalsConfig();
    public static final SepalsConfig PERSISTENT_CONFIG = new SepalsConfig();
    public static Set<String> LOADED_MODS = CollectionFactor.hashSet();
    public static String loadingPlatform = "fabric";
    public static boolean isLithiumLoaded;
    public static boolean isMoonriseLoaded;
    public static boolean isAsyncLoaded;
    public static boolean hasWarned;

    public static void init() {
        LOGGER.info("Sepals '{}' loading on platform '{}'", VERSION, loadingPlatform);
        if (VERSION.endsWith("-SNAPSHOT")) {
            LOGGER.warn("The sepals SNAPSHOT version is more unstable, please do attentions");
        }
        CONFIG.load();
        PERSISTENT_CONFIG.copyFrom(CONFIG);
        CONFIG.print();
    }

    public static void writeConfig() {
        PERSISTENT_CONFIG.write();
    }

    public static boolean isAbleToUseSepalsGetInChunkFunction() {
        if (!hasWarned && (Sepals.isLithiumLoaded || Sepals.isMoonriseLoaded)) {
            if (Sepals.isLithiumLoaded) {
                LOGGER.warn("The server has loaded mod 'lithium', sepals cannot use sepals 'getInChunk' function (but 'isForceEnableSepalsPoi' config will ignore this warning)");
                hasWarned = true;
            }

            if (Sepals.isMoonriseLoaded) {
                LOGGER.warn("The server has loaded mod 'moonrise', sepals cannot use sepals 'getInChunk' function (but 'isForceEnableSepalsPoi' config will ignore this warning)");
                hasWarned = true;
            }

            return false;
        }

        return true;
    }
}
