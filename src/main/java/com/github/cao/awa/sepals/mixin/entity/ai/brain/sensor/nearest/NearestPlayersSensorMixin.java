package com.github.cao.awa.sepals.mixin.entity.ai.brain.sensor.nearest;

import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.Sepals;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestPlayersSensor;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.minecraft.entity.ai.brain.sensor.Sensor.testAttackableTargetPredicate;
import static net.minecraft.entity.ai.brain.sensor.Sensor.testTargetPredicate;

@Mixin(NearestPlayersSensor.class)
public class NearestPlayersSensorMixin {
    @Inject(
            method = "sense",
            at = @At("HEAD"),
            cancellable = true
    )
    public void sense(ServerWorld world, LivingEntity entity, CallbackInfo ci) {
        if (Sepals.enableSepalsVillager) {
            Brain<?> brain = entity.getBrain();

            if (entity instanceof VillagerEntity villager) {
                senseForVillager(villager.getBrain(), world, villager);
            } else {
                boolean canAttackToPlayer = entity instanceof HostileEntity || entity instanceof Angerable;

                collectBasicNearestPlayers(world, entity, brain)
                        .filter(player -> testTargetPredicate(entity, player))
                        .firstOrNull(player -> brain.remember(MemoryModuleType.NEAREST_VISIBLE_PLAYER, player))
                        // If entity are not hostile or anger-able then do not need to test the attackable predicate.
                        // Because this memory only will be used in the entities that can attack to player, such as piglin.
                        .ifPresent(catheter -> {
                            if (!canAttackToPlayer) {
                                catheter.reset();
                            }
                        })
                        .filter(player -> testAttackableTargetPredicate(entity, player))
                        .firstOrNull(player -> brain.remember(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, Optional.ofNullable(player)));
            }
            ci.cancel();
        }
    }

    @Unique
    private void senseForVillager(Brain<VillagerEntity> brain, World world, VillagerEntity entity) {
        collectBasicNearestPlayers(world, entity, brain)
                // This memory in villager has only used in 'GiveGiftsToHeroTask', it required player is villager hero.
                // So let it skip when players doesn't is villager hero.
                .filter(NearestPlayersSensorMixin::isHero)
                .filter(player -> testTargetPredicate(entity, player))
                .firstOrNull(player -> brain.remember(MemoryModuleType.NEAREST_VISIBLE_PLAYER, player));

        // The villager cannot attack to the player, this optional always null.
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, Optional.empty());

    }

    @Unique
    private static boolean isHero(PlayerEntity player) {
        return player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
    }

    @Unique
    private static Catheter<PlayerEntity> collectBasicNearestPlayers(World world, LivingEntity entity, Brain<?> brain) {
        return Catheter.of(world.getPlayers().toArray(PlayerEntity[]::new))
                .arrayGenerator(PlayerEntity[]::new)
                .filter(EntityPredicates.EXCEPT_SPECTATOR)
                .filter(player -> entity.isInRange(player, 16.0))
                .ifPresent(catheter -> {
                    if (Sepals.nearestLivingEntitiesSensorUseQuickSort) {
                        ObjectArrays.quickSort(catheter.dArray(), Comparator.comparingDouble(entity::squaredDistanceTo));
                    } else {
                        Arrays.sort(catheter.dArray(), Comparator.comparingDouble(entity::squaredDistanceTo));
                    }
                })
                .ifPresent(catheter -> brain.remember(MemoryModuleType.NEAREST_PLAYERS, catheter.list()));
    }
}
