package com.github.cao.awa.sepals.config

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter
import com.github.cao.awa.apricot.util.io.IOUtil
import com.github.cao.awa.sepals.config.key.SepalsConfigKey
import com.github.cao.awa.sinuatum.manipulate.Manipulate
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import kotlin.jvm.Throws

class SepalsConfig {
    companion object {
        val LOGGER: Logger = LogManager.getLogger("SepalsConfig")
        val CONFIG_FILE: File = File("config/sepals.json")

        @JvmField
        val FORCE_ENABLE_SEPALS_POI = SepalsConfigKey.create("forceEnableSepalsPoi", false)

        @JvmField
        val ENABLE_SEPALS_VILLAGER = SepalsConfigKey.create("enableSepalsVillager", true)

        @JvmField
        val ENABLE_SEPALS_FROG_LOOK_AT = SepalsConfigKey.create("enableSepalsFrogLookAt", true)

        @JvmField
        val ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR = SepalsConfigKey.create("enableSepalsFrogAttackableSensor", true)

        @JvmField
        val ENABLE_SEPALS_LIVING_TARGET_CACHE = SepalsConfigKey.create("enableSepalsLivingTargetCache", true)

        @JvmField
        val NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT =
            SepalsConfigKey.create("nearestLivingEntitiesSensorUseQuickSort", true)

        @JvmField
        val ENABLE_SEPALS_BIASED_LONG_JUMP_TASK = SepalsConfigKey.create("enableSepalsBiasedLongJumpTask", true)

        @JvmField
        val ENABLE_SEPALS_ENTITIES_CRAMMING = SepalsConfigKey.create("enableSepalsEntitiesCramming", true)
    }

    private val config = JSONObject()

    val isForceEnableSepalsPoi: Boolean get() = getConfig(FORCE_ENABLE_SEPALS_POI)
    val isEnableSepalsVillager: Boolean get() = getConfig(ENABLE_SEPALS_VILLAGER)
    val isEnableSepalsFrogLookAt: Boolean get() = getConfig(ENABLE_SEPALS_FROG_LOOK_AT)
    val isEnableSepalsFrogAttackableSensor: Boolean get() = getConfig(ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR)
    val isEnableSepalsLivingTargetCache: Boolean get() = getConfig(ENABLE_SEPALS_LIVING_TARGET_CACHE)
    val isNearestLivingEntitiesSensorUseQuickSort: Boolean
        get() = getConfig(
            NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT
        )
    val isEnableSepalsBiasedLongJumpTask: Boolean get() = getConfig(ENABLE_SEPALS_BIASED_LONG_JUMP_TASK)
    val isEnableSepalsEntitiesCramming: Boolean get() = getConfig(ENABLE_SEPALS_ENTITIES_CRAMMING)

    @Throws(IllegalArgumentException::class)
    fun <X> setConfig(configKey: SepalsConfigKey<X>, value: X?) {
        this.config[configKey.name] = configKey.checkLimits(checkOrThrow(configKey, value))
    }

    fun <X> setConfig(configKey: SepalsConfigKey<X>, json: JSONObject) {
        this.config[configKey.name] = configKey.checkLimits(checkOrThrow<X>(configKey, json[configKey.name]))
    }

    @Throws(IllegalArgumentException::class)
    fun <X> getConfig(configKey: SepalsConfigKey<X>): X {
        val value = this.config[configKey.name] ?: return configKey.defaultValue
        return checkOrThrow(configKey, value)
    }

    @Throws(IllegalArgumentException::class)
    private fun <X> checkOrThrow(configKey: SepalsConfigKey<*>, value: Any?): X {
        if (value == null) {
            throw NullPointerException("Value should not be null")
        }
        if (configKey.type.isInstance(value) || configKey.type.isAssignableFrom(value.javaClass)) {
            return Manipulate.cast(value)
        }
        throw IllegalArgumentException("Config '" + configKey.name + "' required '" + configKey.type + "' but got '" + value.javaClass + "'")
    }

    fun load() {
        var config: JSONObject? = null
        try {
            config = JSONObject.parse(IOUtil.read(FileReader("config/sepals.json", StandardCharsets.UTF_8)))
        } catch (e: Exception) {
            LOGGER.warn("Config not found, use default values")
        }

        if (config != null) {
            setConfig(FORCE_ENABLE_SEPALS_POI, config)
            setConfig(ENABLE_SEPALS_VILLAGER, config)
            setConfig(ENABLE_SEPALS_FROG_LOOK_AT, config)
            setConfig(ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR, config)
            setConfig(ENABLE_SEPALS_LIVING_TARGET_CACHE, config)
            setConfig(NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT, config)
            setConfig(ENABLE_SEPALS_BIASED_LONG_JUMP_TASK, config)
            setConfig(ENABLE_SEPALS_ENTITIES_CRAMMING, config)
        }
    }

    fun write() {
        try {
            if (!CONFIG_FILE.parentFile.exists()) {
                CONFIG_FILE.parentFile.mkdirs()
            }
            IOUtil.write(
                FileWriter(CONFIG_FILE, StandardCharsets.UTF_8),
                config.toString(JSONWriter.Feature.PrettyFormat)
            )
        } catch (e: Exception) {
            LOGGER.warn("Failed to save config", e)
        }
    }

    fun copyFrom(config: SepalsConfig) {
        setConfig(FORCE_ENABLE_SEPALS_POI, config.isForceEnableSepalsPoi)
        setConfig(ENABLE_SEPALS_VILLAGER, config.isEnableSepalsVillager)
        setConfig(ENABLE_SEPALS_FROG_LOOK_AT, config.isEnableSepalsFrogLookAt)
        setConfig(
            ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR,
            config.isEnableSepalsFrogAttackableSensor
        )
        setConfig(
            ENABLE_SEPALS_LIVING_TARGET_CACHE,
            config.isEnableSepalsLivingTargetCache
        )
        setConfig(
            NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT,
            config.isNearestLivingEntitiesSensorUseQuickSort
        )
        setConfig(
            ENABLE_SEPALS_BIASED_LONG_JUMP_TASK,
            config.isEnableSepalsBiasedLongJumpTask
        )
        setConfig(
            ENABLE_SEPALS_ENTITIES_CRAMMING,
            config.isEnableSepalsEntitiesCramming
        )
    }

    fun print() {
        LOGGER.info("Sepals 'forceEnableSepalsPoi' flag is {}", isForceEnableSepalsPoi)
        LOGGER.info("Sepals 'enableSepalsVillager' flag is {}", isEnableSepalsVillager)
        LOGGER.info("Sepals 'enableSepalsFrogLookAt' flag is {}", isEnableSepalsFrogLookAt)
        LOGGER.info(
            "Sepals 'enableSepalsFrogAttackableSensor' flag is {}",
            isEnableSepalsFrogAttackableSensor
        )
        LOGGER.info(
            "Sepals 'enableSepalsLivingTargetCache' flag is {}",
            isEnableSepalsLivingTargetCache
        )
        LOGGER.info(
            "Sepals 'nearestLivingEntitiesSensorUseQuickSort' flag is {}",
            isNearestLivingEntitiesSensorUseQuickSort
        )
        LOGGER.info(
            "Sepals 'enableSepalsBiasedJumpLongTask' flag is {}",
            isEnableSepalsBiasedLongJumpTask
        )
        LOGGER.info(
            "Sepals 'enableEntitiesCramming' flag is {}",
            isEnableSepalsEntitiesCramming
        )
    }
}
