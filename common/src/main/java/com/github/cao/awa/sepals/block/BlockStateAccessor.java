package com.github.cao.awa.sepals.block;

public interface BlockStateAccessor {
    default boolean isBeds() {
        return sepals$isBed();
    }
    default boolean isClimbale() {
        return sepals$isClimbale();
    }

    boolean sepals$isBed();
    boolean sepals$isClimbale();
}
