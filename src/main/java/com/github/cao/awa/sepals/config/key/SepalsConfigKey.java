package com.github.cao.awa.sepals.config.key;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;

import java.util.List;

public record SepalsConfigKey<T>(String name, Class<T> type, T defaultValue, List<T> limits) {
    public static <X> SepalsConfigKey<X> create(String name, X defaultValue) {
        return new SepalsConfigKey<>(name, Manipulate.cast(defaultValue.getClass()), defaultValue, ApricotCollectionFactor.arrayList());
    }

    @SafeVarargs
    public static <X> SepalsConfigKey<X> create(String name, X defaultValue, X... limits) {
        return new SepalsConfigKey<>(name, Manipulate.cast(defaultValue.getClass()), defaultValue, ApricotCollectionFactor.arrayList(limits));
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
}
