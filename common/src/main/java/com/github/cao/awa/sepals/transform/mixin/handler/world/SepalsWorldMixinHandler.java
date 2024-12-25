package com.github.cao.awa.sepals.transform.mixin.handler.world;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.compatible.mod.SepalsModCompatibles;
import com.github.cao.awa.sepals.transform.mixin.handler.SepalsMixinHandler;

public class SepalsWorldMixinHandler extends SepalsMixinHandler {
    @Override
    public boolean canApply(String mixinGroup, String mixinName, String mixinClassName) {
        return true;
    }

    @Override
    public void postProcess(String mixinGroup, String mixinName, String mixinClassName) {
        if (!mixinGroup.equals("entities")) {
            return;
        }

        if (mixinName.equals("world_entities_cache")) {
            boolean isAsyncLoaded = Sepals.LOADED_MODS.contains(SepalsModCompatibles.ASYNC_MOD_NAME);

            if (isAsyncLoaded) {
                LOGGER.info("Async is loaded, sepals will switch something features to synchronized impl");

                Sepals.isAsyncLoaded = true;
            }
        }
    }
}
