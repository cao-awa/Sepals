package com.github.cao.awa.sepals.mixin.entity;

import com.github.cao.awa.sepals.block.BlockAccessor;
import com.github.cao.awa.sepals.stream.SepalsStream;
import com.github.cao.awa.sepals.world.BlockViewAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(Entity.class)
public class EntityMixin {
    @Redirect(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getStatesInBoxIfLoaded(Lnet/minecraft/util/math/Box;)Ljava/util/stream/Stream;"
            )
    )
    @SuppressWarnings("unchecked")
    public Stream<BlockState> getStatesInBoxIfLoaded(World instance, Box box) {
        Iterator<BlockState> states = ((BlockViewAccessor) instance).getStatesIteratorInBoxIfLoaded(box);

        BlockState successesState = null;

        while (states.hasNext()) {
            BlockState state = states.next();
            BlockAccessor accessor = (BlockAccessor) state.getBlock();

            // The predicate is next to testing in 'noneMatch'.
            // When test not succeed, return the EMPTY_STREAM then skips calls for the noneMatch.
            if (accessor.isFire() || accessor.isLava()) {
                successesState = state;
                break;
            }
        }

        if (successesState == null) {
            return (Stream<BlockState>) SepalsStream.EMPTY_STREAM;
        }
        return Stream.of(successesState);
    }

    @Redirect(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;noneMatch(Ljava/util/function/Predicate;)Z"
            )
    )
    public boolean noneMatch(Stream<BlockState> instance, Predicate<BlockState> predicate) {
        // Can not be EMPTY_STREAM only the predicate succeed, already precalculated in before mixins.
        return instance != SepalsStream.EMPTY_STREAM;
    }
}
