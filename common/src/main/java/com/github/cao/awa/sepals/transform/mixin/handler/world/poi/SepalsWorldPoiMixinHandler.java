package com.github.cao.awa.sepals.transform.mixin.handler.world.poi;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.compatible.mod.SepalsModCompatibles;
import com.github.cao.awa.sepals.transform.mixin.handler.SepalsMixinHandler;
import com.github.cao.awa.sepals.world.poi.SepalsPointOfInterestStorage;
import net.fabricmc.loader.api.ModContainer;

import java.util.Map;
import java.util.Set;

public class SepalsWorldPoiMixinHandler extends SepalsMixinHandler {
    @Override
    public boolean canApply(String mixinGroup, String mixinName, String mixinClassName) {
        if (Sepals.CONFIG.isForceEnableSepalsPoi()) {
            return true;
        }

        if (!mixinGroup.equals("poi")) {
            return false;
        }

        boolean lithiumLoaded = Sepals.LOADED_MODS.contains(SepalsModCompatibles.LITHIUM_MOD_NAME);

        switch (mixinName) {
            case "region_based_storage", "poi_set" -> {
                return !lithiumLoaded;
            }
        }

        return true;
    }

    @Override
    public void postProcess(String mixinGroup, String mixinName, String mixinClassName) {
        if (Sepals.CONFIG.isForceEnableSepalsPoi()) {
            return;
        }

        if (!mixinGroup.equals("poi")) {
            return;
        }

        if (mixinName.equals("region_based_storage")) {
            boolean isLithiumLoaded = Sepals.LOADED_MODS.contains(SepalsModCompatibles.LITHIUM_MOD_NAME);
            boolean isMoonriseLoaded = Sepals.LOADED_MODS.contains(SepalsModCompatibles.MOONRISE_MOD_NAME);
            boolean isAsyncLoaded = Sepals.LOADED_MODS.contains(SepalsModCompatibles.ASYNC_MOD_NAME);

            if (isLithiumLoaded) {
                LOGGER.info("Lithium is loaded, auto-disabling sepals mixin: {}({})", "region_based_storage", mixinClassName);

                SepalsPointOfInterestStorage.onLithiumLoaded();
            }

            if (isMoonriseLoaded) {
                LOGGER.info("Moonrise is loaded, sepals won't intervention chunk loading in 'getInChunk' of POI");

                SepalsPointOfInterestStorage.onMoonriseLoaded();
            }

            if (isAsyncLoaded) {
                LOGGER.info("Async is loaded, sepals will switch something features to synchronized impl");

                Sepals.isAsyncLoaded = true;
            }
        }
    }
}
