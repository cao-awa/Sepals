package com.github.cao.awa.sepals.mixin.world;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.Sepals;
import com.github.cao.awa.sepals.entity.cramming.SepalsEntityCrammingStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(World.class)
public abstract class WorldMixin {
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
            String cacheKey = boxToString(box);

            List<Entity> result = SepalsEntityCrammingStorage.cached(cacheKey);
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
            SepalsEntityCrammingStorage.cache(boxToString(box), cir.getReturnValue());
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

            Set<Entity> cached = SepalsEntityCrammingStorage.cachedGetByType(cacheKey);

            if (cached == null) {
                return;
            }

            Catheter<T> catheter = Catheter.of(
                    (Set<T>) cached
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
            SepalsEntityCrammingStorage.cacheGetByType(boxToString(box), cir.getReturnValue());
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
