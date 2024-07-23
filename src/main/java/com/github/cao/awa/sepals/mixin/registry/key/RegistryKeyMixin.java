package com.github.cao.awa.sepals.mixin.registry.key;

import com.github.cao.awa.sepals.registry.key.ReferenceLocatedRegistryKey;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RegistryKey.class)
public abstract class RegistryKeyMixin<T> implements ReferenceLocatedRegistryKey<T> {
    @Unique
    private RegistryEntry.Reference<TagKey<T>> reference;

    @Override
    public void sepals$setReference(RegistryEntry.Reference<TagKey<T>> reference) {
        this.reference = reference;
    }

    @Override
    public boolean sepals$isIn(Object reference) {
        return this.reference == reference;
    }
}
