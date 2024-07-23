package com.github.cao.awa.sepals.mixin.registry;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.sepals.registry.key.ReferenceLocatedRegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Mixin(RegistryEntry.Reference.class)
public class RegistryEntryReferenceMixin<T> {
    @Inject(
            method = "setTags",
            at = @At("HEAD")
    )
    @SuppressWarnings("unchecked")
    public void setTags(Collection<TagKey<T>> tags, CallbackInfo ci) {
        for (TagKey<T> tag : tags) {
            ((ReferenceLocatedRegistryKey<T>) tag.registry()).sepals$setReference(
                    (RegistryEntry.Reference<TagKey<T>>)(Object) this
            );
        }
    }


    @Redirect(
            method = "isIn",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"
            )
    )
    @SuppressWarnings("unchecked")
    public boolean isIn(Set<TagKey<T>> ignored, Object tag) {
        return ((ReferenceLocatedRegistryKey<T>) ((TagKey<T>)tag).registry()).sepals$isIn(this);
    }
}
