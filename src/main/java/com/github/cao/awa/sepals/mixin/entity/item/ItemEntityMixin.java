package com.github.cao.awa.sepals.mixin.entity.item;

import com.github.cao.awa.sepals.item.BoxedItemEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

// TODO preparing to optimization items.
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Unique
    private boolean isFireImmuneCached = false;
    @Unique
    private boolean fireImmune = false;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract boolean canMerge();

    @Shadow
    protected abstract void tryMerge(ItemEntity other);

    @Inject(
            method = "tryMerge()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void tryMerge(CallbackInfo ci) {
        if (canMerge()) {
            getBoxedEntities(
                    getWorld(),
                    getBoundingBox().expand(0.5, 0.0, 0.5),
                    itemEntity -> {
                        tryMerge(itemEntity);
                        return isRemoved();
                    }
            );
        }
        ci.cancel();
    }

    @Unique
    public void getBoxedEntities(World world, Box box, Predicate<ItemEntity> invalidate) {
        if (world instanceof BoxedItemEntities entities) {
            if (entities.canSetEntities()) {
                entities.setEntities(world.getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class), box, entity -> entity != (Object) this && ((ItemEntityAccessor) entity).invokeCanMerge()));
            }

            for (ItemEntity entity : entities.entities()) {
                ItemStack stack = entity.getStack();

                if (stack.getMaxCount() == stack.getCount()) {
                    entities.invalidate(entity);
                    continue;
                }

                if (!entity.getBoundingBox().intersects(box)) {
                    continue;
                }

                if (invalidate.test(entity)) {
                    // Invalidate this item, because it no longer can be to other items.
                    entities.invalidate((ItemEntity) (Object) this);
                    break;
                }
            }
        }
    }

    @Inject(
            method = "isFireImmune",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fireImmune(CallbackInfoReturnable<Boolean> cir) {
        if (this.isFireImmuneCached) {
            cir.setReturnValue(this.fireImmune);
        }
    }

    @Inject(
            method = "isFireImmune",
            at = @At("RETURN")
    )
    private void cacheFireImmune(CallbackInfoReturnable<Boolean> cir) {
        this.isFireImmuneCached = true;
        this.fireImmune = cir.getReturnValue();
    }
}
