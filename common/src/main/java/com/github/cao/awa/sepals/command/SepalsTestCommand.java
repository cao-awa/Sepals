package com.github.cao.awa.sepals.command;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.config.SepalsConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SepalsTestCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("sepalstest")
                        .executes(context -> {
                            int enabledConfigs = Sepals.CONFIG.collectEnabled().size();

                            context.getSource().sendFeedback(
                                    () -> Text.of("Sepals '" + Sepals.VERSION + "' successfully loaded, platform is " + Sepals.loadingPlatform + ", has " + enabledConfigs + " configs enabled"),
                                    false
                            );
                            return 0;
                        })
                        .requires(context -> context.hasPermissionLevel(4))
                        .then(
                                literal("explosion").then(
                                        argument("count", IntegerArgumentType.integer()).executes(context -> {
                                           int explosionCount = IntegerArgumentType.getInteger(context, "count");
                                           ServerCommandSource source = context.getSource();

                                            PlayerEntity player = source.getPlayer();

                                            if (player != null) {
                                                BlockPos pos = player.getBlockPos();
                                                World world = player.getWorld();

                                                for (int i = 0; i < explosionCount; i++) {
                                                    world.createExplosion(
                                                            player,
                                                            pos.getX(),
                                                            pos.getY(),
                                                            pos.getZ(),
                                                            5.0F,
                                                            World.ExplosionSourceType.TRIGGER
                                                    );
                                                }
                                            }

                                            return 0;
                                        })
                                )
                        )
        );
    }
}
