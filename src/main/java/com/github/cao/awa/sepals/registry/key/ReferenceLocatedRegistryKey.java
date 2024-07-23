package com.github.cao.awa.sepals.registry.key;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

public interface ReferenceLocatedRegistryKey<T> {
    void sepals$setReference(RegistryEntry.Reference<TagKey<T>> reference);
    boolean sepals$isIn(Object reference);
}
