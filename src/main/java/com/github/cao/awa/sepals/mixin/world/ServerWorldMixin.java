package com.github.cao.awa.sepals.mixin.world;

import com.github.cao.awa.sepals.item.BoxedItemEntities;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.EntityList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements BoxedItemEntities {
    @Unique
    private final Map<Integer, ItemEntity> entities = new Int2ObjectRBTreeMap<>();
    @Unique
    private boolean canSetEntity = true;

    @Unique
    public void sepals$addEntity(ItemEntity entity) {
        this.entities.put(entity.getId(), entity);
    }

    @Unique
    public boolean sepals$isEmpty() {
        return this.entities.isEmpty();
    }

    @Override
    public boolean sepals$canSetEntities() {
        return isEmpty() && this.canSetEntity;
    }

    @Unique
    public void sepals$setEntities(List<ItemEntity> entities) {
        for (ItemEntity entity : entities) {
            this.entities.put(entity.getId(), entity);
        }
        this.canSetEntity = false;
    }

    @Unique
    public void sepals$clearItemBoxed() {
        this.entities.clear();
        this.canSetEntity = true;
    }

    @Unique
    public void sepals$invalidate(ItemEntity entity) {
        this.entities.remove(entity.getId());
    }

    @Unique
    public Collection<ItemEntity> sepals$entities() {
        return this.entities.values();
    }

    @Unique
    public Collection<ItemEntity> sepals$entitiesAndInvalidate(ItemEntity entity) {
        Collection<ItemEntity> items = this.entities.values();

        if (!items.contains(entity)) {
            this.entities.remove(entity.getId());
        }

        return items;
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V"
            )
    )
    public void resetItemEntities(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        clearItemBoxed();
    }
}
