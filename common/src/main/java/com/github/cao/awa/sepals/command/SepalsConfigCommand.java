package com.github.cao.awa.sepals.command;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.config.SepalsConfig;
import com.github.cao.awa.sepals.config.key.SepalsConfigKey;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class SepalsConfigCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("sepals")
                        .executes(context -> {
                            int enabledConfigs = Sepals.CONFIG.collectEnabled().size();

                            context.getSource().sendFeedback(
                                    () -> Text.of("Sepals '" + Sepals.VERSION + "' successfully loaded, platform is " + Sepals.loadingPlatform + ", has " + enabledConfigs + " configs enabled"),
                                    false
                            );
                            return 0;
                        })
                        .requires(context -> context.hasPermissionLevel(4))
                        .then(createConfigNode(SepalsConfig.FORCE_ENABLE_SEPALS_POI))
                        .then(createConfigNode(SepalsConfig.ENABLE_SEPALS_VILLAGER))
                        .then(createConfigNode(SepalsConfig.ENABLE_SEPALS_FROG_LOOK_AT))
                        .then(createConfigNode(SepalsConfig.ENABLE_SEPALS_FROG_ATTACKABLE_SENSOR))
                        .then(createConfigNode(SepalsConfig.ENABLE_SEPALS_LIVING_TARGET_CACHE))
                        .then(createConfigNode(SepalsConfig.NEAREST_LIVING_ENTITIES_SENSOR_USE_QUICK_SORT))
                        .then(createConfigNode(SepalsConfig.ENABLE_SEPALS_BIASED_LONG_JUMP_TASK))
                        .then(createConfigNode(SepalsConfig.ENABLE_SEPALS_ENTITIES_CRAMMING))
                        .then(createConfigNode(SepalsConfig.ENABLE_SEPALS_ITEM_MERGE))
                        .then(createConfigNode(SepalsConfig.ENABLE_SEPALS_QUICK_CAN_BE_PUSH_BY_ENTITY_PREDICATE))
        );
    }

    private static int changeConfigTemporary(CommandContext<ServerCommandSource> context, SepalsConfigKey key, BiFunction<CommandContext<ServerCommandSource>, String, Boolean> argument) {
        return changeConfig(context, key, argument, true);
    }

    private static int changeConfig(CommandContext<ServerCommandSource> context, SepalsConfigKey key, BiFunction<CommandContext<ServerCommandSource>, String, Boolean> argument) {
        return changeConfig(context, key, argument, false);
    }

    private static int changeConfig(CommandContext<ServerCommandSource> context, SepalsConfigKey key, BiFunction<CommandContext<ServerCommandSource>, String, Boolean> argument, boolean temporary) {
        boolean value = argument.apply(context, key.name());
        Sepals.CONFIG.setConfig(key, value);
        if (temporary) {
            context.getSource().sendFeedback(
                    () -> Text.of("Config '" + key.name() + "' is '" + value + "' temporarily"),
                    true
            );
        } else {
            Sepals.PERSISTENT_CONFIG.setConfig(key, value);
            context.getSource().sendFeedback(
                    () -> Text.of("Config '" + key.name() + "' is '" + value + "' now"),
                    true
            );

            context.getSource().getServer().execute(Sepals::writeConfig);
        }
        return 0;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createConfigNode(SepalsConfigKey key) {
        return createConfigNode(key, BoolArgumentType::bool, BoolArgumentType::getBool);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createConfigNode(SepalsConfigKey key, Supplier<ArgumentType<Boolean>> argumentType, BiFunction<CommandContext<ServerCommandSource>, String, Boolean> argument) {
        String configName = key.name();

        return CommandManager.literal(configName)
                .executes(context -> {
                    context.getSource().sendFeedback(
                            () -> Text.of("Config '" + configName + "' is '" + Sepals.CONFIG.getConfig(key) + "' now"),
                            false
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
