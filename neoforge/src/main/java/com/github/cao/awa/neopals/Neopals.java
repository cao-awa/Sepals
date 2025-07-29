package com.github.cao.awa.neopals;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.command.SepalsConfigCommand;
import com.github.cao.awa.sepals.command.SepalsTestCommand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;
import java.util.Set;

@Mod("sepals")
public final class Neopals {
    public Neopals(IEventBus modEventBus) {
        modEventBus.addListener(FMLCommonSetupEvent.class, this::onCommonSetup);

        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, this::registerConfig);
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        Sepals.LOADED_MODS = Optional.of(ModList.get().getSortedMods()).map(mods -> {
            Set<String> modsSet = ApricotCollectionFactor.hashSet();
            for (ModContainer mod : mods) {
                modsSet.add(mod.getModId());
            }
            return modsSet;
        }).orElseGet(ApricotCollectionFactor::hashSet);

        Sepals.loadingPlatform = "neoforge";

        Sepals.init();
    }

    public void registerConfig(RegisterCommandsEvent event) {
        Sepals.LOGGER.info("Registering commands");

        SepalsConfigCommand.register(event.getDispatcher());
        SepalsTestCommand.register(event.getDispatcher());
    }
}
