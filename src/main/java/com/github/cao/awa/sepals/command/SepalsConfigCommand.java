package com.github.cao.awa.sepals.command;

import com.github.cao.awa.sepals.Sepals;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class SepalsConfigCommand {
    public static void register(MinecraftServer server) {
        server.getCommandManager()
                .getDispatcher()
                .register(
                        CommandManager.literal("sepals").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4)).then(
                                CommandManager.literal("weightTable").then(
                                        CommandManager.argument("enableSepalsWeightTable", BoolArgumentType.bool()).executes(context -> {
                                            Sepals.enableSepalsWeightTable = BoolArgumentType.getBool(context, "enableSepalsWeightTable");
                                            if (Sepals.enableSepalsWeightTable) {
                                                context.getSource().sendFeedback(() -> {
                                                    return Text.of("The sepals weight table is enabled");
                                                }, true);
                                            } else {
                                                context.getSource().sendFeedback(() -> {
                                                    return Text.of("The sepals weight table is disabled");
                                                }, true);
                                            }
                                            return 0;
                                        })
                                )
                        ).then(CommandManager.literal("entitiesCramming").then(
                                CommandManager.argument("enableEntitiesCramming", BoolArgumentType.bool()).executes(context -> {
                                    Sepals.enableEntitiesCramming = BoolArgumentType.getBool(context, "enableEntitiesCramming");
                                    if (Sepals.enableEntitiesCramming) {
                                        context.getSource().sendFeedback(() -> {
                                            return Text.of("The sepals entities cramming is enabled");
                                        }, true);
                                    } else {
                                        context.getSource().sendFeedback(() -> {
                                            return Text.of("The sepals entities cramming is disabled");
                                        }, true);
                                    }
                                    return 0;
                                })
                        )).then(CommandManager.literal("optimizedBiasedLongJump").then(
                                CommandManager.argument("enableOptimizedBiasedLongJump", BoolArgumentType.bool()).executes(context -> {
                                    Sepals.enableSepalsBiasedJumpLongTask = BoolArgumentType.getBool(context, "enableOptimizedBiasedLongJump");
                                    if (Sepals.enableSepalsBiasedJumpLongTask) {
                                        context.getSource().sendFeedback(() -> {
                                            return Text.of("The sepals biased long jump task is enabled, settings of sepals weight table will be ignored");
                                        }, true);
                                    } else {
                                        context.getSource().sendFeedback(() -> {
                                            return Text.of("The sepals biased long jump task is disabled");
                                        }, true);
                                    }
                                    return 0;
                                })
                        )).then(CommandManager.literal("nearestLivingEntitiesSensorUseQuickSort").then(
                                CommandManager.argument("nearestLivingEntitiesSensorUseQuickSort", BoolArgumentType.bool()).executes(context -> {
                                    Sepals.nearestLivingEntitiesSensorUseQuickSort = BoolArgumentType.getBool(context, "nearestLivingEntitiesSensorUseQuickSort");
                                    if (Sepals.nearestLivingEntitiesSensorUseQuickSort) {
                                        context.getSource().sendFeedback(() -> {
                                            return Text.of("The nearest living entities sensor will use quick sort to sort");
                                        }, true);
                                    } else {
                                        context.getSource().sendFeedback(() -> {
                                            return Text.of("The nearest living entities sensor will use tim sort to sort");
                                        }, true);
                                    }
                                    return 0;
                                })
                        ))
                );
    }
}
