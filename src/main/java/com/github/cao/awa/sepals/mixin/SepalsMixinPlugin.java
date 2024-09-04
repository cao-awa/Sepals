package com.github.cao.awa.sepals.mixin;

import com.alibaba.fastjson2.JSONObject;
import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.apricot.util.io.IOUtil;
import com.github.cao.awa.sepals.transform.mixin.config.SepalsMixinConfig;
import com.github.cao.awa.sepals.transform.mixin.config.handler.SepalsMixinHandlerConfig;
import com.github.cao.awa.sepals.transform.mixin.handler.SepalsMixinHandler;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class SepalsMixinPlugin implements IMixinConfigPlugin {
    private static final Map<String, ModContainer> MODS = Optional.of(FabricLoader.getInstance().getAllMods()).map(mods -> {
        Map<String, ModContainer> modsMap = ApricotCollectionFactor.hashMap();
        for (ModContainer mod : mods) {
            modsMap.put(mod.getMetadata().getId(), mod);
        }
        return modsMap;
    }).orElseGet(ApricotCollectionFactor::hashMap);
    private static SepalsMixinConfig config;

    @Override
    public void onLoad(String mixinPackage) {
        if (config == null) {
            SepalsMixinHandler.registerDefaultHandlers();
            config = Manipulate.make((x) -> {
                IMixinService service = MixinService.getService();
                InputStream resource = service.getResourceAsStream("sepals.mixin-handlers.json");
                if (resource == null) {
                    throw new IllegalArgumentException("The specified resource 'sepals.mixin-handlers.json' was invalid or could not be read");
                }
                try {
                    return SepalsMixinConfig.create(JSONObject.parse(IOUtil.read(new InputStreamReader(resource))));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }).get();
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        mixinClassName = mixinClassName.replace("/", ".");

        return config.ifHasHandlerConfig(
                mixinClassName,
                handlerConfig -> {
                    SepalsMixinHandler.appendPostProcesses(() -> handlerConfig.handler().postProcess(
                            MODS,
                            handlerConfig.group(),
                            handlerConfig.name(),
                            handlerConfig.className()
                    ));
                    return handlerConfig.handler().canApply(
                            MODS,
                            handlerConfig.group(),
                            handlerConfig.name(),
                            handlerConfig.className()
                    );
                },
                () -> true
        );
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
