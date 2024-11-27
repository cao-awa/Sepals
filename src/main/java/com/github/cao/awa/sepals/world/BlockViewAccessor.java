package com.github.cao.awa.sepals.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Box;

import java.util.Iterator;

public interface BlockViewAccessor {
    default Iterator<BlockState> getStatesIteratorInBox(Box box) {
        return sepals$getStatesIteratorInBox(box);
    }

    Iterator<BlockState> sepals$getStatesIteratorInBox(Box box);

    default Iterator<BlockState> getStatesIteratorInBoxIfLoaded(Box box) {
        return sepals$getStatesIteratorInBoxIfLoaded(box);
    }

    Iterator<BlockState> sepals$getStatesIteratorInBoxIfLoaded(Box box);
}
