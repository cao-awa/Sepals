package com.github.cao.awa.sepals.mixin.world;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.item.BoxedEntitiesCache;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(World.class)
public abstract class WorldMixin implements BoxedEntitiesCache {
    @Unique
    private Map<String, List<Entity>> entities = ApricotCollectionFactor.hashMap();
    @Unique
    private Map<String, List<Entity>> getByTypeEntities = ApricotCollectionFactor.hashMap();

    @Unique
    public List<Entity> cachedGetByType(String box) {
        if (this.getByTypeEntities == null) {
            this.getByTypeEntities = ApricotCollectionFactor.hashMap();
        }
        return this.getByTypeEntities.get(box);
    }

    @Unique
    public void cacheGetByType(String box, List<Entity> entities) {
        if (this.getByTypeEntities == null) {
            this.getByTypeEntities = ApricotCollectionFactor.hashMap();
        }
        this.getByTypeEntities.put(box, entities);
    }

    @Unique
    public void sepals$cache(Box box, List<Entity> entities) {
        if (this.entities == null) {
            this.entities = ApricotCollectionFactor.hashMap();
        }
        this.entities.put(boxToString(box), entities);
    }

    @Unique
    public List<Entity> sepals$cached(Box box) {
        if (this.entities == null) {
            this.entities = ApricotCollectionFactor.hashMap();
        }
        return this.entities.get(boxToString(box));
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
//    @Inject(
//            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;ZLnet/minecraft/particle/ParticleEffect;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/registry/entry/RegistryEntry;)Lnet/minecraft/world/explosion/Explosion;",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, boolean particles, ParticleEffect particle, ParticleEffect emitterParticle, RegistryEntry<SoundEvent> soundEvent, CallbackInfoReturnable<Explosion> cir) {
//        Explosion.DestructionType destructionType = switch(explosionSourceType.ordinal()) {
//            case 0 -> Explosion.DestructionType.KEEP;
//            case 1 -> getDestructionType(GameRules.BLOCK_EXPLOSION_DROP_DECAY);
//            case 2 -> getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)
//                    ? getDestructionType(GameRules.MOB_EXPLOSION_DROP_DECAY)
//                    : Explosion.DestructionType.KEEP;
//            case 3 -> getDestructionType(GameRules.TNT_EXPLOSION_DROP_DECAY);
//            case 4 -> Explosion.DestructionType.TRIGGER_BLOCK;
//            default -> throw new MatchException(null, null);
//        };
//        Explosion explosion = new SepalExplosion((World)(Object)this, entity, damageSource, behavior, x, y, z, power, createFire, destructionType, particle, emitterParticle, soundEvent);
//        explosion.collectBlocksAndDamageEntities();
//        explosion.affectWorld(particles);
//        cir.setReturnValue(explosion);
//    }

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
            String cacheKey = boxToString(box);

            List<Entity> cached = cachedGetByType(cacheKey);

            if (cached == null) {
                return;
            }

            Catheter<T> catheter = Catheter.of(
                    (List<T>) cached
            );

            List<T> result = (List<T>) catheter
                    .filter(predicate)
                    .varyTo(filter::downcast)
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
            cacheGetByType(boxToString(box), cir.getReturnValue());
        }
    }

    @Unique
    private static String boxToString(Box box) {
        String minX = boxPosToString(box.minX);
        String minY = boxPosToString(box.minY);
        String minZ = boxPosToString(box.minZ);
        String maxX = boxPosToString(box.maxX);
        String maxY = boxPosToString(box.maxY);
        String maxZ = boxPosToString(box.maxZ);

        return minX + minY + minZ + maxX + maxY + maxZ;
    }

    @Unique
    private static String boxPosToString(double pos) {
        return Double.toString(pos);
    }
}
