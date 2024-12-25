package com.github.cao.awa.sepals.transform.mixin.handler;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.sepals.transform.mixin.handler.world.poi.SepalsWorldPoiMixinHandler;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SepalsMixinHandler {
    private static final List<Runnable> postProcesses = ApricotCollectionFactor.arrayList();
    private static final Map<String, SepalsMixinHandler> handlers = ApricotCollectionFactor.hashMap();

    public static final Logger LOGGER = LogManager.getLogger("SepalsMixinHandler");

    public abstract boolean canApply(Set<String> mods, String mixinGroup, String mixinName, String mixinClassName);

    public abstract void postProcess(Set<String> mods, String mixinGroup, String mixinName, String mixinClassName);

    public static void registerHandler(String group, SepalsMixinHandler handler) {
        handlers.put(group, handler);
    }

    public static void appendPostProcesses(Runnable handler) {
        postProcesses.add(handler);
    }

    public static void startPostProcess() {
        postProcesses.forEach(Runnable::run);
        postProcesses.clear();
    }

    @SuppressWarnings("unchecked")
    public static <H extends SepalsMixinHandler> H handler(String group) {
        return (H) handlers.get(group);
    }

    public static void registerDefaultHandlers() {
        if (handlers.isEmpty()) {
            registerHandler("poi", new SepalsWorldPoiMixinHandler());
        }
    }
}
