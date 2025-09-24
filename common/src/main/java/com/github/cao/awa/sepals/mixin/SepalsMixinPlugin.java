package com.github.cao.awa.sepals.mixin;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.config.key.SepalsConfigKey;
import com.github.cao.awa.sepals.transform.mixin.config.SepalsMixinConfig;
import com.github.cao.awa.sepals.transform.mixin.handler.SepalsMixinHandler;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import com.github.cao.awa.sinuatum.util.io.IOUtil;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger LOGGER = LogManager.getLogger("SepalsMixinPlugin");
    private static SepalsMixinConfig config;

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("Sepals plugin for version '{}' is loading", Sepals.VERSION);

        if (config == null) {
            SepalsMixinHandler.registerDefaultHandlers();
            config = Manipulate.supplyLater(() -> {
                IMixinService service = MixinService.getService();
                InputStream resource = service.getResourceAsStream("sepals.mixin-handlers.json");
                if (resource == null) {
                    throw new IllegalArgumentException("The specified resource 'sepals.mixin-handlers.json' was invalid or could not be read");
                }
                try {
                    return SepalsMixinConfig.create(JsonHelper.deserialize(IOUtil.read(new InputStreamReader(resource))));
                } catch (IOException e) {
                    LOGGER.warn(e);
                }

                return null;
            }).catching(Exception.class, Exception::printStackTrace).get();
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
                            handlerConfig.group(),
                            handlerConfig.name(),
                            handlerConfig.className()
                    ));
                    LOGGER.info("Handling: {} in group {} / {}", handlerConfig.group(), handlerConfig.name(), handlerConfig.className());
                    return handlerConfig.handler().canApply(
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
        Sepals.LOGGER.info("Doing sepals configs change actions for {}:{}", targetClassName, mixinClassName);
        Sepals.CONFIG.collectEnabled().forEach(SepalsConfigKey::doChangeAction);
    }
}
