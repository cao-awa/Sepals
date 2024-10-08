package com.github.cao.awa.sepals.mixin.world;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

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
}
