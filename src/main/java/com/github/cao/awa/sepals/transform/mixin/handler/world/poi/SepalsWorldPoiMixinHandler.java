package com.github.cao.awa.sepals.transform.mixin.handler.world.poi;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.compatible.mod.SepalsModCompatibles;
import com.github.cao.awa.sepals.transform.mixin.handler.SepalsMixinHandler;
import com.github.cao.awa.sepals.world.poi.SepalsPointOfInterestStorage;
import net.fabricmc.loader.api.ModContainer;

import java.util.Map;

public class SepalsWorldPoiMixinHandler extends SepalsMixinHandler {
    @Override
    public boolean canApply(Map<String, ModContainer> mods, String mixinGroup, String mixinName, String mixinClassName) {
        if (Sepals.forceEnableSepalsPoi) {
            return true;
        }

        LOGGER.info("Handling: {} in group {} / {}", mixinName, mixinGroup, mixinClassName);

        if (!mixinGroup.equals("poi")) {
            return false;
        }

        boolean lithiumLoaded = mods.containsKey(SepalsModCompatibles.LITHIUM_MOD_NAME);

        switch (mixinName) {
            case "region_based_storage", "poi_set" -> {
                return !lithiumLoaded;
            }
        }

        return true;
    }

    @Override
    public void postProcess(Map<String, ModContainer> mods, String mixinGroup, String mixinName, String mixinClassName) {
        if (Sepals.forceEnableSepalsPoi) {
            return;
        }

        if (!mixinGroup.equals("poi")) {
            return;
        }

        switch (mixinName) {
            case "region_based_storage" -> {
                boolean isLithiumLoaded = mods.containsKey(SepalsModCompatibles.LITHIUM_MOD_NAME);

                if (isLithiumLoaded) {
                    LOGGER.info("Lithium is loaded, auto-disabling sepals mixin: {}({})", "region_based_storage", mixinClassName);

                    SepalsPointOfInterestStorage.onLithiumLoaded();
                }
            }
        }
    }
}
