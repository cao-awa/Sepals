package com.github.cao.awa.sepals.mixin.entity;

import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.entity.cramming.SepalsEntityCrammingStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Redirect(
            method = "tickCramming",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    public List<Entity> getCachedCrammingEntities(World instance, @Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
        if (Sepals.CONFIG.isEnableSepalsEntitiesCramming()) {
            String minX = boxPosToString(box.minX);
            String minY = boxPosToString(box.minY);
            String minZ = boxPosToString(box.minZ);
            String maxX = boxPosToString(box.maxX);
            String maxY = boxPosToString(box.maxY);
            String maxZ = boxPosToString(box.maxZ);

            String cacheKey = minX + minY + minZ + maxX + maxY + maxZ;

            List<Entity> result = SepalsEntityCrammingStorage.cached(cacheKey);
            if (result == null) {
                result = instance.getOtherEntities(except, box);
                SepalsEntityCrammingStorage.cache(cacheKey, result);
            }
            return result;
        } else {
            return instance.getOtherEntities(except, box);
        }
    }

    @Unique
    private static String boxPosToString(double pos) {
        return Integer.toString((int) (pos * Sepals.CONFIG.getEntitiesCrammingAccuracy()));
    }
}
