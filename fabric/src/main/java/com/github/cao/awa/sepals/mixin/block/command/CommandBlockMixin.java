package com.github.cao.awa.sepals.mixin.block.command;

import com.github.cao.awa.sepals.command.SepalsDebugCommand;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandBlock.class)
public class CommandBlockMixin {
    @Inject(
            method = "scheduledTick",
            at = @At("HEAD")
    )
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        SepalsDebugCommand.commandBlocks.add(pos);
    }
}
