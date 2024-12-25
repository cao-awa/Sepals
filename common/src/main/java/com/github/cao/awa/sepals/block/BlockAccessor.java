package com.github.cao.awa.sepals.block;

public interface BlockAccessor {
    default boolean isLava() {
        return sepals$isLava();
    }

    boolean sepals$isLava();

    default boolean isFire() {
        return sepals$isFire();
    }

    boolean sepals$isFire();
}
