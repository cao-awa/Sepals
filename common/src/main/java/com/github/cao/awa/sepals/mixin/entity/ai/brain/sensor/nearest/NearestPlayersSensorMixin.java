package com.github.cao.awa.sepals.mixin.entity.ai.brain.sensor.nearest;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.Sepals;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestPlayersSensor;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import static net.minecraft.entity.ai.brain.sensor.Sensor.testAttackableTargetPredicate;
import static net.minecraft.entity.ai.brain.sensor.Sensor.testTargetPredicate;

@Mixin(NearestPlayersSensor.class)
public abstract class NearestPlayersSensorMixin {
    @Inject(
            method = "sense",
            at = @At("HEAD"),
            cancellable = true
    )
    public void sense(ServerWorld world, LivingEntity entity, CallbackInfo ci) {
        if (Sepals.CONFIG.isEnableSepalsVillager()) {
            Brain<?> brain = entity.getBrain();

            Catheter<PlayerEntity> players = collectBasicNearestPlayers(world, entity, brain);

            boolean isVillager = entity instanceof VillagerEntity;
            boolean canAttackToPlayer = entity instanceof HostileEntity || entity instanceof Angerable;

            // This memory in villager has only used in the 'GiveGiftsToHeroTask', It required player is villager hero.
            // So let it skip when players don't be the villager hero.
            if (isVillager) {
                players.filter(NearestPlayersSensorMixin::isHero);
            }

            players.filter(player -> testTargetPredicate(world, entity, player));
            players.firstOrNull(player -> brain.remember(MemoryModuleType.NEAREST_VISIBLE_PLAYER, player));
            // If the entity is not hostile or anger-able, then do not need to test the attack-able predicate.
            // Because this memory only will be used in the entities that can attack to player, such as piglin.
            if (canAttackToPlayer) {
                players.filter(player -> testAttackableTargetPredicate(world, entity, player))
                        .firstOrNull(player -> brain.remember(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, Optional.ofNullable(player)));
            }

            ci.cancel();
        }
    }

    @Unique
    private Catheter<PlayerEntity> collectBasicNearestPlayers(World world, LivingEntity entity, Brain<?> brain) {
        return Catheter.of(world.getPlayers().toArray(PlayerEntity[]::new))
                .arrayGenerator(PlayerEntity[]::new)
                .filter(EntityPredicates.EXCEPT_SPECTATOR)
                .filter(player -> entity.isInRange(player, 16.0D))
                .ifPresent(catheter -> {
                    if (Sepals.CONFIG.isNearestLivingEntitiesSensorUseQuickSort()) {
                        ObjectArrays.quickSort(catheter.dArray(), Comparator.comparingDouble(entity::squaredDistanceTo));
                    } else {
                        Arrays.sort(catheter.dArray(), Comparator.comparingDouble(entity::squaredDistanceTo));
                    }
                })
                .ifPresent(catheter -> brain.remember(MemoryModuleType.NEAREST_PLAYERS, catheter.list()));
    }

    @Unique
    private static boolean isHero(PlayerEntity player) {
        return player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
    }
}
