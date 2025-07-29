package com.github.cao.awa.sepals.mixin.world;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.item.BoxedEntitiesCache;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(World.class)
public abstract class WorldMixin implements BoxedEntitiesCache {
    @Unique
    private static final Supplier<Map<Long, List<Entity>>> mapSupplier = () -> {
        if (Sepals.isAsyncLoaded) {
            return Collections.synchronizedMap(new Long2ObjectOpenHashMap<>());
        }
        return new Long2ObjectOpenHashMap<>();
    };

    @Unique
    private Map<Long, List<Entity>> entities = Manipulate.supply(mapSupplier::get);
    @Unique
    private Map<Long, List<Entity>> getByTypeEntities = Manipulate.supply(mapSupplier::get);

    @Unique
    public List<Entity> cachedGetByType(long hashCode) {
        if (this.getByTypeEntities == null) {
            this.getByTypeEntities = mapSupplier.get();
        }
        return this.getByTypeEntities.get(hashCode);
    }

    @Unique
    public void cacheGetByType(long hashCode, List<Entity> entities) {
        if (this.getByTypeEntities == null) {
            this.getByTypeEntities = mapSupplier.get();
        }
        this.getByTypeEntities.put(hashCode, entities);
    }

    @Unique
    public void sepals$cache(Box box, List<Entity> entities) {
        if (this.entities == null) {
            this.entities = mapSupplier.get();
        }
        this.entities.put(boxHashCode(box), entities);
    }

    @Unique
    public List<Entity> sepals$cached(Box box) {
        if (this.entities == null) {
            this.entities = mapSupplier.get();
        }
        return this.entities.get(boxHashCode(box));
    }

    @Unique
    public void sepals$clearCache() {
        if (this.entities != null) {
            this.entities.clear();
        }
        if (this.getByTypeEntities != null) {
            this.getByTypeEntities.clear();
        }
    }
//    @Shadow protected abstract Explosion.DestructionType getDestructionType(GameRules.Key<GameRules.BooleanRule> gameRuleKey);
//
//    @Shadow public abstract GameRules getGameRules();
//

    @Inject(
            method = "getOtherEntities",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getOtherEntities(Entity except, Box box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
        if (Sepals.CONFIG.isEnableSepalsEntitiesCramming()) {
            List<Entity> result = cached(box);
            if (result != null) {
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(
            method = "getOtherEntities",
            at = @At("RETURN")
    )
    public void cacheOtherEntities(Entity except, Box box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
        if (Sepals.CONFIG.isEnableSepalsEntitiesCramming()) {
            cache(box, cir.getReturnValue());
        }
    }

    @Inject(
            method = "getEntitiesByType",
            at = @At("HEAD"),
            cancellable = true
    )
    @SuppressWarnings("unchecked")
    public <T extends Entity> void getEntitiesByType(TypeFilter<T, ? extends T> filter, Box box, Predicate<? super T> predicate, CallbackInfoReturnable<List<T>> cir) {
        if (Sepals.CONFIG.isEnableSepalsEntitiesCramming()) {
            long cacheKey = boxHashCode(box);

            List<Entity> cached = cachedGetByType(cacheKey);

            if (cached == null) {
                return;
            }

            Catheter<T> catheter = Catheter.of(
                    (List<T>) cached
            );

            List<T> result = (List<T>) catheter
                    .filter(predicate)
                    .varyTo(entity -> Manipulate.supply(() -> filter.downcast(entity)))
                    .exists()
                    .list();

            if (!result.isEmpty()) {
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(
            method = "getEntitiesByType",
            at = @At("RETURN")
    )
    public void cacheEntitiesByType(TypeFilter<Entity, ? extends Entity> filter, Box box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
        if (Sepals.CONFIG.isEnableSepalsEntitiesCramming()) {
            cacheGetByType(boxHashCode(box), cir.getReturnValue());
        }
    }

    @Unique
    private static long boxHashCode(Box box) {
        int minHash = Arrays.hashCode(new double[]{box.minX, box.minY, box.minZ});
        int maxHash = Arrays.hashCode(new double[]{box.maxX, box.maxY, box.maxZ});

        return (((long) minHash & 0xFFFFFFFFL) | ((long) maxHash << 32) & 0xFFFFFFFF00000000L);
    }

    @Unique
    private static long directBoxHashCode(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        long result = 1;
        long bitsMinX = Double.doubleToLongBits(maxX);
        long bitsMinY = Double.doubleToLongBits(maxY);
        long bitsMinZ = Double.doubleToLongBits(maxZ);
        long bitsMaxX = Double.doubleToLongBits(minX);
        long bitsMaxY = Double.doubleToLongBits(minY);
        long bitsMaxZ = Double.doubleToLongBits(minZ);
        result = 31 * result + (bitsMinX ^ (bitsMinX >>> 32));
        result = 31 * result + (bitsMinY ^ (bitsMinY >>> 32));
        result = 31 * result + (bitsMinZ ^ (bitsMinZ >>> 32));
        result = 31 * result + (bitsMaxX ^ (bitsMaxX >>> 32));
        result = 31 * result + (bitsMaxY ^ (bitsMaxY >>> 32));
        result = 31 * result + (bitsMaxZ ^ (bitsMaxZ >>> 32));
        return result;
    }
}
