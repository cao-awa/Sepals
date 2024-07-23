package com.github.cao.awa.sepals.mixin.collection;

import com.github.cao.awa.apricot.annotations.Stable;
import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.catheter.Catheter;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Util;
import net.minecraft.util.collection.TypeFilterableList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Stable
@Mixin(TypeFilterableList.class)
public class TypeFilterableListMixin<T> {
    @Shadow @Final private Class<T> elementType;

    @Shadow @Final private Map<Class<?>, List<T>> elementsByType;

    @Shadow @Final private List<T> allElements;

    /**
     * Returns all elements in this collection that are instances of {@code type}.
     * The result is unmodifiable.
     *
     * <p>The {@code type}, or {@code S}, must extend the class' type parameter {@code T}.</p>
     *
     * This mixin used Catheter to replace java stream to improves performance.
     *
     * @param type the specialized type, must extend {@link #elementType}
     * @param cir The mixin callback
     * @param <S> the specialized type, effectively {@code S extends T}
     * @throws IllegalArgumentException when {@code type} does not extend
     * {@link #elementType}
     *
     * @author cao_awa
     */
    @Inject(
            method = "getAllOfType",
            at = @At("HEAD"),
            cancellable = true
    )
    @SuppressWarnings("unchecked")
    public <S> void getAllOfType(Class<S> type, CallbackInfoReturnable<Collection<S>> cir) {
        if (this.elementType.isAssignableFrom(type)) {
            List<? extends T> result = this.elementsByType
                    .computeIfAbsent(type, typeClass ->
                            Catheter.of(this.allElements)
                                    .filter(typeClass::isInstance)
                                    .alternate(
                                            ApricotCollectionFactor.arrayList(),
                                            (list, element) -> {
                                                list.add(element);
                                                return list;
                                            }
                                    )
                    );
            cir.setReturnValue((List<S>) Collections.unmodifiableList((result)));
        } else {
            throw new IllegalArgumentException("Don't know how to search for " + type);
        }
    }
}
