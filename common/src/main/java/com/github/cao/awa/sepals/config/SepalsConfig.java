package com.github.cao.awa.sepals.config;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.cao.awa.sepals.config.key.SepalsConfigKey;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import com.github.cao.awa.sinuatum.util.io.IOUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;

public class SepalsConfig {
    private static final Logger LOGGER = LogManager.getLogger("SepalsConfig");
    private static final File CONFIG_FILE = new File("config/sepals.json");
    public static final SepalsConfigKey<Boolean> FORCE_ENABLE_SEPALS_POI = SepalsConfigKey.create("forceEnableSepalsPoi", false);
    public static final SepalsConfigKey<Boolean> ENABLE_SEPALS_VILLAGER = SepalsConfigKey.create("enableSepalsVillager", true);
    public static final SepalsConfigKey<Boolean> ENABLE_SEPALS_FROG_LOOK_AT = SepalsConfigKey.create("enableSepalsFrogLookAt", true);
    public static final SepalsConfigKey<Boolean> ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR = SepalsConfigKey.create("enableSepalsFrogAttackableSensor", true);
    public static final SepalsConfigKey<Boolean> ENABLE_SEPALS_LIVING_TARGET_CACHE = SepalsConfigKey.create("enableSepalsLivingTargetCache", true);
    public static final SepalsConfigKey<Boolean> NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT = SepalsConfigKey.create("nearestLivingEntitiesSensorUseQuickSort", true);
    public static final SepalsConfigKey<Boolean> ENABLE_SEPALS_BIASED_LONG_JUMP_TASK = SepalsConfigKey.create("enableSepalsBiasedLongJumpTask", true);
    public static final SepalsConfigKey<Boolean> ENABLE_SEPALS_ENTITIES_CRAMMING = SepalsConfigKey.create("enableSepalsEntitiesCramming", true);

    private final JSONObject config = new JSONObject();

    public boolean isForceEnableSepalsPoi() {
        return getConfig(FORCE_ENABLE_SEPALS_POI);
    }

    public boolean isEnableSepalsVillager() {
        return getConfig(ENABLE_SEPALS_VILLAGER);
    }

    public boolean isEnableSepalsFrogLookAt() {
        return getConfig(ENABLE_SEPALS_FROG_LOOK_AT);
    }

    public boolean isEnableSepalsFrogAttackableSensor() {
        return getConfig(ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR);
    }

    public boolean isEnableSepalsLivingTargetCache() {
        return getConfig(ENABLE_SEPALS_LIVING_TARGET_CACHE);
    }

    public boolean isNearestLivingEntitiesSensorUseQuickSort() {
        return getConfig(NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT);
    }

    public boolean isEnableSepalsBiasedLongJumpTask() {
        return getConfig(ENABLE_SEPALS_BIASED_LONG_JUMP_TASK);
    }

    public boolean isEnableSepalsEntitiesCramming() {
        return getConfig(ENABLE_SEPALS_ENTITIES_CRAMMING);
    }

    public <X> void setConfig(SepalsConfigKey<X> configKey, X value) {
        this.config.put(configKey.name(), configKey.checkLimits(checkOrThrow(configKey, value)));
    }

    public <X> void setConfig(SepalsConfigKey<X> configKey, JSONObject json) {
        this.config.put(configKey.name(), configKey.checkLimits(checkOrThrow(configKey, json.get(configKey.name()))));
    }

    public <X> X getConfig(@NotNull SepalsConfigKey<X> configKey) {
        Object value = this.config.get(configKey.name());
        if (value == null) {
            return configKey.defaultValue();
        }
        return checkOrThrow(configKey, value);
    }

    @NotNull
    private static <X> X checkOrThrow(@NotNull SepalsConfigKey<X> configKey, Object value) {
        if (value == null) {
            throw new NullPointerException("Config value should not be null");
        }
        if (configKey.type().isInstance(value) || configKey.type().isAssignableFrom(value.getClass())) {
            return Manipulate.cast(value);
        }
        throw new IllegalArgumentException("Config '" + configKey.name() + "' required '" + configKey.type() + "' but got '" + value.getClass() + "'");
    }

    public void load() {
        try {
            final JSONObject config = JSONObject.parse(IOUtil.read(new FileReader(CONFIG_FILE, StandardCharsets.UTF_8)));

            setConfig(FORCE_ENABLE_SEPALS_POI, config);
            setConfig(ENABLE_SEPALS_VILLAGER, config);
            setConfig(ENABLE_SEPALS_FROG_LOOK_AT, config);
            setConfig(ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR, config);
            setConfig(ENABLE_SEPALS_LIVING_TARGET_CACHE, config);
            setConfig(NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT, config);
            setConfig(ENABLE_SEPALS_BIASED_LONG_JUMP_TASK, config);
            setConfig(ENABLE_SEPALS_ENTITIES_CRAMMING, config);
        } catch (Exception e) {
            LOGGER.warn("Config not found, use default values", e);
            write();
        }
    }

    public void write() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            IOUtil.write(
                    new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8),
                    this.config.toString(JSONWriter.Feature.PrettyFormat)
            );
        } catch (Exception e) {
            LOGGER.warn("Failed to save config", e);
        }
    }

    public void copyFrom(@NotNull SepalsConfig config) {
        setConfig(FORCE_ENABLE_SEPALS_POI, config.isForceEnableSepalsPoi());
        setConfig(ENABLE_SEPALS_VILLAGER, config.isEnableSepalsVillager());
        setConfig(ENABLE_SEPALS_FROG_LOOK_AT, config.isEnableSepalsFrogLookAt());
        setConfig(ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR, config.isEnableSepalsFrogAttackableSensor());
        setConfig(ENABLE_SEPALS_LIVING_TARGET_CACHE, config.isEnableSepalsLivingTargetCache());
        setConfig(NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT, config.isNearestLivingEntitiesSensorUseQuickSort());
        setConfig(ENABLE_SEPALS_BIASED_LONG_JUMP_TASK, config.isEnableSepalsBiasedLongJumpTask());
        setConfig(ENABLE_SEPALS_ENTITIES_CRAMMING, config.isEnableSepalsEntitiesCramming());
    }

    public void print() {
        LOGGER.info("Sepals 'forceEnableSepalsPoi' flag is {}", isForceEnableSepalsPoi());
        LOGGER.info("Sepals 'enableSepalsVillager' flag is {}", isEnableSepalsVillager());
        LOGGER.info("Sepals 'enableSepalsFrogLookAt' flag is {}", isEnableSepalsFrogLookAt());
        LOGGER.info("Sepals 'enableSepalsFrogAttackableSensor' flag is {}", isEnableSepalsFrogAttackableSensor());
        LOGGER.info("Sepals 'enableSepalsLivingTargetCache' flag is {}", isEnableSepalsLivingTargetCache());
        LOGGER.info("Sepals 'nearestLivingEntitiesSensorUseQuickSort' flag is {}", isNearestLivingEntitiesSensorUseQuickSort());
        LOGGER.info("Sepals 'enableSepalsBiasedJumpLongTask' flag is {}", isEnableSepalsBiasedLongJumpTask());
        LOGGER.info("Sepals 'enableEntitiesCramming' flag is {}", isEnableSepalsEntitiesCramming());
    }

    public Set<SepalsConfigKey<?>> collectEnabled() {
        Set<SepalsConfigKey<?>> enabled = CollectionFactor.hashSet();

        if (isForceEnableSepalsPoi()) {
            enabled.add(FORCE_ENABLE_SEPALS_POI);
        }

        if (isEnableSepalsVillager()) {
            enabled.add(ENABLE_SEPALS_VILLAGER);
        }

        if (isEnableSepalsFrogLookAt()) {
            enabled.add(ENABLE_SEPALS_FROG_LOOK_AT);
        }

        if (isEnableSepalsFrogAttackableSensor()) {
            enabled.add(ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR);
        }

        if (isEnableSepalsLivingTargetCache()) {
            enabled.add(ENABLE_SEPALS_LIVING_TARGET_CACHE);
        }

        if (isNearestLivingEntitiesSensorUseQuickSort()) {
            enabled.add(NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT);
        }

        if (isEnableSepalsBiasedLongJumpTask()) {
            enabled.add(ENABLE_SEPALS_BIASED_LONG_JUMP_TASK);
        }

        if (isEnableSepalsEntitiesCramming()) {
            enabled.add(ENABLE_SEPALS_ENTITIES_CRAMMING);
        }

        return enabled;
    }
}
