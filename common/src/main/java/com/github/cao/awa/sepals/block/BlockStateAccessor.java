package com.github.cao.awa.sepals.block;

public interface BlockStateAccessor {
    default boolean isBeds() {
        return sepals$isBed();
    }

    boolean sepals$isBed();
}
