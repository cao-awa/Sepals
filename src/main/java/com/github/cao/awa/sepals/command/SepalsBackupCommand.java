package com.github.cao.awa.sepals.command;

import com.github.cao.awa.sepals.Sepals;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SepalsBackupCommand {
    private static final TriConsumer<ServerCommandSource, Text, Boolean> messageFeedback = (source, text, isError) -> {
        if (isError) {
            source.sendError(text);
        } else {
            source.sendFeedback(() -> text, true);
        }
    };

    public static void register(MinecraftServer xServer) {
        xServer.getCommandManager()
                .getDispatcher()
                .register(
                        CommandManager.literal("backup").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4)).then(
                                CommandManager.literal("make").executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    makeBackup(
                                            context,
                                            null,
                                            false,
                                            (text, isError) -> messageFeedback.accept(source, text, isError)
                                    );
                                    return 0;
                                }).then(CommandManager.argument("tips", StringArgumentType.string()).executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    makeBackup(
                                            context,
                                            StringArgumentType.getString(context, "tips"),
                                            false,
                                            (text, isError) -> messageFeedback.accept(source, text, isError)
                                    );
                                    return 0;
                                }).then(CommandManager.literal("rebase").executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    makeBackup(
                                            context,
                                            StringArgumentType.getString(context, "tips"),
                                            true,
                                            (text, isError) -> messageFeedback.accept(source, text, isError)
                                    );
                                    return 0;
                                })))
                        ).then(CommandManager.literal("list").executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Sepals.backupCenter.showBackups((text, isError) -> messageFeedback.accept(source, text, isError));

                            if (Sepals.backupCenter.backupCount() > 5) {
                                MutableText seeMoreNotice = MutableText.of(PlainTextContent.of("Has only shown newest 5 backups here, see more use page feature"));
                                seeMoreNotice.setStyle(seeMoreNotice.getStyle().withColor(Formatting.YELLOW));
                                messageFeedback.accept(source, seeMoreNotice, false);
                            }

                            return 0;
                        }).then(CommandManager.argument("backupsListPage", IntegerArgumentType.integer(0, Integer.MAX_VALUE)).executes(context -> {
                            ServerCommandSource source = context.getSource();

                            int pages = IntegerArgumentType.getInteger(context, "backupsListPage");
                            int count = Sepals.backupCenter.backupCount();

                            Sepals.backupCenter.showBackups(
                                    (text, isError) -> messageFeedback.accept(source, text, isError),
                                    count - pages * 5
                            );

                            return 0;
                        }))).then(CommandManager.literal("rollback").then(
                                CommandManager.argument("rollbackTarget", IntegerArgumentType.integer(0, Integer.MAX_VALUE)).executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    int rollbackTarget = IntegerArgumentType.getInteger(context, "rollbackTarget");

                                    Sepals.backupCenter.wantRollback(rollbackTarget);

                                    source.getServer().stop(false);

                                    return 0;
                                })
                        )).then(CommandManager.literal("rebase").then(
                                CommandManager.argument("rebaseTarget", IntegerArgumentType.integer(0, Integer.MAX_VALUE)).executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    int rollbackTarget = IntegerArgumentType.getInteger(context, "rebaseTarget");

                                    Sepals.backupCenter.doRebase(rollbackTarget, source.getServer(), (text, isError) -> messageFeedback.accept(source, text, isError));

                                    return 0;
                                })
                        ))
                );
    }

    private static void makeBackup(CommandContext<ServerCommandSource> context, String tips, boolean rebase, BiConsumer<Text, Boolean> feedback) {
        new Thread(() -> {
            ServerCommandSource source = context.getSource();
            MinecraftServer server = source.getServer();

            int newBackupId = Sepals.backupCenter.makeBackup(Set.of(),
                    tips,
                    server,
                    (text, isError) -> messageFeedback.accept(source, text, isError)
            );

            if (rebase) {
                Sepals.backupCenter.doRebase(newBackupId, server, feedback);
            }
        }).start();
    }
}
