package com.github.cao.awa.sepals.command;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.config.SepalsConfig;
import com.github.cao.awa.sepals.config.key.SepalsConfigKey;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class SepalsConfigCommand {
    public static void register(MinecraftServer server) {
        server.getCommandManager()
                .getDispatcher()
                .register(
                        CommandManager.literal("sepals")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                                .then(createBoolConfigNode(SepalsConfig.FORCE_ENABLE_SEPALS_POI))
                                .then(createBoolConfigNode(SepalsConfig.ENABLE_SEPALS_VILLAGER))
                                .then(createBoolConfigNode(SepalsConfig.ENABLE_SEPALS_FROG_LOOK_AT))
                                .then(createBoolConfigNode(SepalsConfig.ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR))
                                .then(createBoolConfigNode(SepalsConfig.ENABLE_SEPALS_LIVING_TARGET_CACHE))
                                .then(createBoolConfigNode(SepalsConfig.NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT))
                                .then(createBoolConfigNode(SepalsConfig.ENABLE_SEPALS_BIASED_LONG_JUMP_TASK))
                                .then(createBoolConfigNode(SepalsConfig.ENABLE_SEPALS_ENTITIES_CRAMMING))
                );
    }

    private static <X> int changeConfigTemporary(CommandContext<ServerCommandSource> context, SepalsConfigKey<X> key, BiFunction<CommandContext<ServerCommandSource>, String, X> argument) {
        return changeConfig(context, key, argument, true);
    }

    private static <X> int changeConfig(CommandContext<ServerCommandSource> context, SepalsConfigKey<X> key, BiFunction<CommandContext<ServerCommandSource>, String, X> argument) {
        return changeConfig(context, key, argument, false);
    }

    private static <X> int changeConfig(CommandContext<ServerCommandSource> context, SepalsConfigKey<X> key, BiFunction<CommandContext<ServerCommandSource>, String, X> argument, boolean temporary) {
        X value = argument.apply(context, key.name());
        Sepals.CONFIG.setConfig(key, value);
        if (temporary) {
            context.getSource().sendFeedback(
                    () -> Text.of("Config '" + SepalsConfig.ENABLE_SEPALS_ENTITIES_CRAMMING.name() + "' is '" + value + "' temporarily"),
                    true
            );
        } else {
            Sepals.PERSISTENT_CONFIG.setConfig(key, value);
            context.getSource().sendFeedback(
                    () -> Text.of("Config '" + SepalsConfig.ENABLE_SEPALS_ENTITIES_CRAMMING.name() + "' is '" + value + "' now"),
                    true
            );

            context.getSource().getServer().execute(Sepals::writeConfig);
        }
        return 0;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createBoolConfigNode(SepalsConfigKey<Boolean> key) {
        return createConfigNode(key, BoolArgumentType::bool, BoolArgumentType::getBool);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createIntConfigNode(SepalsConfigKey<Integer> key) {
        return createConfigNode(key, IntegerArgumentType::integer, IntegerArgumentType::getInteger);
    }

    private static <X> LiteralArgumentBuilder<ServerCommandSource> createConfigNode(SepalsConfigKey<X> key, Supplier<ArgumentType<X>> argumentType, BiFunction<CommandContext<ServerCommandSource>, String, X> argument) {
        String configName = key.name();

        return CommandManager.literal(configName)
                .executes(context -> {
                    context.getSource().sendFeedback(
                            () -> Text.of("Config '" + configName + "' is '" + Sepals.CONFIG.getConfig(key) + "' now"),
                            true
                    );
                    return 0;
                })
                .then(
                        CommandManager.argument(configName, argumentType.get())
                                .executes(context -> changeConfig(context, key, argument))
                                .then(CommandManager.literal("temporary")
                                        .executes(context -> changeConfigTemporary(context, key, argument))
                                )
                );
    }
}
