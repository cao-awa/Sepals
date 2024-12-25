package com.github.cao.awa.neopals;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.command.SepalsConfigCommand;
import dev.architectury.event.events.common.LifecycleEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;

@Mod("neopals")
public final class Neopals {
    public Neopals(IEventBus modEventBus) {
        Sepals.LOADED_MODS = Optional.of(ModList.get().getSortedMods()).map(mods -> {
            Set<String> modsSet = ApricotCollectionFactor.hashSet();
            for (ModContainer mod : mods) {
                modsSet.add(mod.getModId());
            }
            return modsSet;
        }).orElseGet(ApricotCollectionFactor::hashSet);

        Sepals.loadingPlatform = "neoforge";

        Sepals.init();

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerConfig(RegisterCommandsEvent event) {
        Sepals.LOGGER.info("Registering commands");

        SepalsConfigCommand.register(event.getDispatcher());
    }
}
