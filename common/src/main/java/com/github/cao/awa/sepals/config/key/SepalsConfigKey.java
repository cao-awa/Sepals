package com.github.cao.awa.sepals.config.key;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import org.apache.commons.lang3.function.Consumers;

import java.util.List;
import java.util.function.Consumer;

public record SepalsConfigKey<T>(String name, Class<T> type, T value, Consumer<T> onChangeAction, List<T> limits) {
    public static <X> SepalsConfigKey<X> create(String name, X defaultValue) {
        return new SepalsConfigKey<>(name, Manipulate.cast(defaultValue.getClass()), defaultValue, Consumers.nop(), ApricotCollectionFactor.arrayList());
    }

    public static <X> SepalsConfigKey<X> create(String name, X defaultValue, Consumer<X> onChangeAction) {
        return new SepalsConfigKey<>(name, Manipulate.cast(defaultValue.getClass()), defaultValue, onChangeAction, ApricotCollectionFactor.arrayList());
    }

    @SafeVarargs
    public static <X> SepalsConfigKey<X> create(String name, X defaultValue, X... limits) {
        return new SepalsConfigKey<>(name, Manipulate.cast(defaultValue.getClass()), defaultValue, Consumers.nop(), ApricotCollectionFactor.arrayList(limits));
    }

    @SafeVarargs
    public static <X> SepalsConfigKey<X> create(String name, X defaultValue, Consumer<X> onChangeAction, X... limits) {
        return new SepalsConfigKey<>(name, Manipulate.cast(defaultValue.getClass()), defaultValue, onChangeAction, ApricotCollectionFactor.arrayList(limits));
    }

    @SafeVarargs
    public final SepalsConfigKey<T> withLimits(T... limits) {
        this.limits.clear();
        this.limits.addAll(ApricotCollectionFactor.arrayList(limits));
        return this;
    }

    public T checkLimits(T value) {
        if (this.limits.isEmpty() || this.limits.contains(value)) {
            return value;
        }
        throw new IllegalStateException("Unexpected config value '" + value + "', the config '" + this.name + "' only allow these values: " + this.limits);
    }

    public void doChangeAction() {
        this.onChangeAction.accept(this.value);
    }
}
