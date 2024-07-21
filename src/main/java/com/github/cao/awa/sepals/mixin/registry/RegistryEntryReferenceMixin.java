package com.github.cao.awa.sepals.mixin.registry;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;

@Mixin(RegistryEntry.Reference.class)
public class RegistryEntryReferenceMixin<T> {
    @Unique
    private final Map<TagKey<T>, Object> tags = ApricotCollectionFactor.hashMap();

    @Inject(
            method = "setTags",
            at = @At("HEAD")
    )
    public void setTags(Collection<TagKey<T>> tags, CallbackInfo ci) {
        this.tags.clear();
        for (TagKey<T> tag : tags) {
            this.tags.put(tag, true);
        }
    }


    @Inject(
            method = "isIn",
            at = @At("HEAD"),
            cancellable = true
    )
    public void isIn(TagKey<T> tag, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.tags.get(tag) != null);
    }
}
