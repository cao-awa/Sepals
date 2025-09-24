package com.github.cao.awa.sepals.block.state;

public interface BlockStateTagAccessor {
    default boolean isClimbable() {
        return sepals$isClimbable();
    }

    boolean sepals$isClimbable();

    boolean sepals$isClimbableInitialized();
}
