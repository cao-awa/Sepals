package com.github.cao.awa.sepals.consumer;

import java.util.Objects;

@FunctionalInterface
public interface BooleanConsumer {
    BooleanConsumer NOP = (t) -> {};

    static BooleanConsumer nop() {
        return NOP;
    }

    void accept(boolean var1);

    default BooleanConsumer then(BooleanConsumer after) {
        Objects.requireNonNull(after);
        return (t) -> {
            this.accept(t);
            after.accept(t);
        };
    }
}

