package com.github.cao.awa.sepals.mixin.entity.track;

import com.github.cao.awa.apricot.annotations.Stable;
import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.sepals.mixin.collection.TypeFilterableListAccessor;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

@Stable
@Mixin(EntityTrackingSection.class)
public abstract class EntityTrackingSectionMixin<T extends EntityLike>  {
    @Shadow @Final private TypeFilterableList<T> collection;

    @Inject(
            method = "forEach(Lnet/minecraft/util/math/Box;Lnet/minecraft/util/function/LazyIterationConsumer;)Lnet/minecraft/util/function/LazyIterationConsumer$NextIteration;",
            at = @At("HEAD"),
            cancellable = true
    )
    @SuppressWarnings("unchecked")
    public void forEach(Box box, LazyIterationConsumer<T> consumer, CallbackInfoReturnable<LazyIterationConsumer.NextIteration> cir) {
        // Access and clone the elements to match the vanilla behavior.
        List<T> elements = ApricotCollectionFactor.arrayList(
                ((TypeFilterableListAccessor<T>) this.collection).getAllElements()
        );
        int endIndex = elements.size();

        for (int index = 0; ; index++) {
            if (index == endIndex) {
                break;
            }
            T entityLike = elements.get(index);
            if (entityLike.getBoundingBox().intersects(box) && consumer.accept(entityLike).shouldAbort()) {
                cir.setReturnValue(LazyIterationConsumer.NextIteration.ABORT);
                return;
            }
        }

        cir.setReturnValue(LazyIterationConsumer.NextIteration.CONTINUE);
    }

    @Inject(
            method = "forEach(Lnet/minecraft/util/TypeFilter;Lnet/minecraft/util/math/Box;Lnet/minecraft/util/function/LazyIterationConsumer;)Lnet/minecraft/util/function/LazyIterationConsumer$NextIteration;",
            at = @At("HEAD"),
            cancellable = true
    )
    @SuppressWarnings("unchecked")
    public <U extends T> void forEach(TypeFilter<T, U> type, Box box, LazyIterationConsumer<? super U> consumer, CallbackInfoReturnable<LazyIterationConsumer.NextIteration> cir) {
        List<? extends T> collection = (List<? extends T>) this.collection.getAllOfType(type.getBaseClass());
        if (collection.isEmpty()) {
            cir.setReturnValue(LazyIterationConsumer.NextIteration.CONTINUE);
        } else {
            int endIndex = collection.size();

            for (int index = 0; ; index++) {
                if (index == endIndex) {
                    break;
                }
                T entityLike = collection.get(index);
                U entityLike2 = type.downcast(entityLike);
                if (entityLike2 == null) {
                    continue;
                }
                if (entityLike.getBoundingBox().intersects(box) && consumer.accept(entityLike2).shouldAbort()) {
                    cir.setReturnValue(LazyIterationConsumer.NextIteration.ABORT);
                    return;
                }
            }
        }
    }
}
