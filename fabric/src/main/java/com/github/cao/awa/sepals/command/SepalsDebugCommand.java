package com.github.cao.awa.sepals.command;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.catheter.action.BooleanPredicate;
import com.github.cao.awa.sepals.entity.ai.brain.DetailedDebuggableTask;
import com.github.cao.awa.sepals.entity.ai.brain.TaskDelegate;
import com.github.cao.awa.sepals.entity.ai.task.composite.SepalsCompositeTask;
import com.github.cao.awa.sepals.entity.ai.task.composite.SepalsTaskStatus;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Memory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class SepalsDebugCommand {
    public static final Set<BlockPos> commandBlocks = ApricotCollectionFactor.hashSet();

    public static void register(MinecraftServer server) {
        server.getCommandManager()
                .getDispatcher()
                .register(
                        CommandManager.literal("debugs").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4)).then(
                                        CommandManager.literal("tasks").then(
                                                CommandManager.literal("all").then(
                                                                CommandManager.argument("target", EntityArgumentType.entity())
                                                                        .executes(context -> showTasks(context, b -> true))
                                                                        .then(CommandManager.literal("onlyRunning")
                                                                                .executes(context -> showTasks(context, b -> b))
                                                                        )
                                                                        .then(CommandManager.literal("onlyStopped")
                                                                                .executes(context -> showTasks(context, b -> !b))
                                                                        )
                                                        )
                                                        .then(CommandManager.literal("special"))
                                        )
                                )
                                .then(CommandManager.literal("memories").then(
                                        CommandManager.literal("all").then(
                                                        CommandManager.argument("target", EntityArgumentType.entity())
                                                                .executes(context -> showMemories(context, b -> true))
                                                                .then(CommandManager.literal("onlyPresent")
                                                                        .executes(context -> showMemories(context, b -> b))
                                                                )
                                                                .then(CommandManager.literal("onlyEmpty")
                                                                        .executes(context -> showMemories(context, b -> !b))
                                                                )
                                                )
                                                .then(CommandManager.literal("special"))
                                ))
                                .then(CommandManager.literal("locate").then(
                                        CommandManager.literal("commandBlock").executes(SepalsDebugCommand::showCommandBlocks)
                                ))
                );
    }

    private static int showCommandBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        if (!commandBlocks.isEmpty()) {
            source.sendFeedback(() -> Text.literal("Found " + commandBlocks.size() + " command blocks: ").styled(style -> style.withColor(Formatting.YELLOW)), true);
            source.sendFeedback(() -> Text.literal(commandBlocks.stream().map(pos -> "<x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ() + ">").toList().toString()).styled(style -> style.withColor(Formatting.AQUA)), true);
        } else {
            source.sendError(Text.literal("Current not any command block running in the world"));
            return -1;
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    private static int showTasks(CommandContext<ServerCommandSource> context, BooleanPredicate showWhenRunning) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Entity entity = EntityArgumentType.getEntity(context, "target");

        if (entity == null) {
            source.sendError(Text.of("Unable to select to an entity"));

            return -1;
        }

        if (entity instanceof LivingEntity livingEntity) {
            source.sendFeedback(() -> Text.literal("Tasks of " + entity.getUuid()).styled(style -> style.withColor(Formatting.YELLOW)), true);

            ((TaskDelegate<LivingEntity>) livingEntity.getBrain()).sepals$tasks().each(task -> {
                if (task instanceof DetailedDebuggableTask detailedTask) {
                    boolean running = ((DetailedDebuggableTask) task).alwaysRunning() || SepalsTaskStatus.isRunning(task);
                    if (showWhenRunning.test(running)) {
                        if (task instanceof SepalsCompositeTask<? super LivingEntity> compositeTask) {
                            source.sendFeedback(() -> Text.literal("* " + compositeTask.information()).styled(style -> style.withColor(Formatting.DARK_GREEN)), true);
                            compositeTask.sepals$tasks().each(compositedTask -> {
                                if (compositedTask instanceof DetailedDebuggableTask compositedDetailedTask) {
                                    source.sendFeedback(() -> Text.literal("# " + (compositedDetailedTask.alwaysRunning() || SepalsTaskStatus.isStopped(compositedTask) ? "+ " : "- ")).styled(style -> style.withColor(Formatting.GRAY)), true);
                                    source.sendFeedback(() -> Text.literal(compositedDetailedTask.information()).styled(style -> style.withColor(Formatting.LIGHT_PURPLE)), true);
                                } else {
                                    source.sendFeedback(() -> Text.literal("# " + (SepalsTaskStatus.isStopped(compositedTask) ? "+ " : "- ")).styled(style -> style.withColor(Formatting.GRAY)), true);
                                    source.sendFeedback(() -> Text.literal(compositedTask.toString()).styled(style -> style.withColor(Formatting.LIGHT_PURPLE)), true);
                                }
                            });
                        } else {
                            source.sendFeedback(() -> Text.literal((running ? "+ " : "- ")).styled(style -> style.withColor(Formatting.GRAY)), true);
                            source.sendFeedback(() -> Text.literal(detailedTask.information()).styled(style -> style.withColor(running ? Formatting.AQUA : Formatting.RED)), true);
                        }
                    }
                } else {
                    boolean running = SepalsTaskStatus.isRunning(task);
                    if (showWhenRunning.test(running)) {
                        source.sendFeedback(() -> Text.literal((running ? "+ " : "- ") + task).styled(style -> style.withColor(running ? Formatting.AQUA : Formatting.RED)), true);
                    }
                }
            });
        } else {
            source.sendError(Text.of("Unable to select to an entity as living entity"));
        }

        return 0;
    }

    private static int showMemories(CommandContext<ServerCommandSource> context, BooleanPredicate showWhenPresent) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Entity entity = EntityArgumentType.getEntity(context, "target");

        if (entity == null) {
            source.sendError(Text.of("Unable to select to an entity"));

            return -1;
        }

        if (entity instanceof LivingEntity livingEntity) {
            source.sendFeedback(() -> Text.literal("Memories of " + entity.getUuid()).styled(style -> style.withColor(Formatting.YELLOW)), true);

            livingEntity.getBrain().getMemories().forEach((key, value) -> {
                boolean present = value.isPresent();
                Memory<?> memory = value.orElse(null);
                if (showWhenPresent.test(present)) {
                    source.sendFeedback(() -> Text.literal((present ? "+ " : "- ") + key.toString() + (present ? ": " + memory : "")).styled(style -> style.withColor(present ? Formatting.AQUA : Formatting.RED)), true);
                }
            });
        } else {
            source.sendError(Text.of("Unable to select to an entity as living entity"));
        }

        return 0;
    }
}
