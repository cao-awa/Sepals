package com.github.cao.awa.sepals.config.key;

import com.github.cao.awa.sepals.consumer.BooleanConsumer;

public record SepalsConfigKey(String name, boolean value, BooleanConsumer onChangeAction) {
    public static SepalsConfigKey create(String name, boolean defaultValue) {
        return new SepalsConfigKey(name, defaultValue, BooleanConsumer.nop());
    }

    public static SepalsConfigKey create(String name, boolean defaultValue, BooleanConsumer onChangeAction) {
        return new SepalsConfigKey(name, defaultValue, onChangeAction);
    }

    public void doChangeAction() {
        this.onChangeAction.accept(this.value);
    }
}
