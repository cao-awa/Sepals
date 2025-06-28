package com.github.cao.awa.fapals;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.fapals.command.SepalsDebugCommand;
import com.github.cao.awa.fapals.command.config.SepalsFabricConfigCommand;
import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.command.SepalsConfigCommand;
import com.github.cao.awa.sepals.transform.mixin.handler.SepalsMixinHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;
import java.util.Set;

public class Fapals implements ModInitializer {
    @Override
    public void onInitialize() {
        Sepals.loadingPlatform = "fabric";

        Sepals.LOADED_MODS = Optional.of(FabricLoader.getInstance().getAllMods()).map(mods -> {
            Set<String> modsSet = ApricotCollectionFactor.hashSet();
            for (ModContainer mod : mods) {
                modsSet.add(mod.getMetadata().getId());
            }
            return modsSet;
        }).orElseGet(ApricotCollectionFactor::hashSet);

        SepalsMixinHandler.startPostProcess();

        Sepals.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Sepals.LOGGER.info("Registering commands");
            SepalsDebugCommand.register(server);
            SepalsConfigCommand.register(server.getCommandManager().getDispatcher());
        });
    }
}