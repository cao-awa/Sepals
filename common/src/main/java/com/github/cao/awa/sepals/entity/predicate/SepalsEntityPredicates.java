package com.github.cao.awa.sepals.entity.predicate;

import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;

import java.util.function.Predicate;

public class SepalsEntityPredicates {
    public static Predicate<Entity> quickCanBePushedBy(Entity currentEntity) {
        AbstractTeam currentTeam = currentEntity.getScoreboardTeam();
        AbstractTeam.CollisionRule currentCollisionRule;
        if (currentTeam == null) {
            currentCollisionRule = AbstractTeam.CollisionRule.ALWAYS;
        } else {
            currentCollisionRule = currentTeam.getCollisionRule();

            if (currentCollisionRule == AbstractTeam.CollisionRule.NEVER) {
                return Predicates.alwaysFalse();
            }
        }

        boolean currentEntityNoPushOwnTeam = currentCollisionRule != AbstractTeam.CollisionRule.PUSH_OWN_TEAM;
        boolean currentEntityNoPushOtherTeams = currentCollisionRule != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS;

        boolean currentEntityIsClient = currentEntity.getWorld().isClient;

        return otherEntity -> {
            if (otherEntity.isSpectator() || !otherEntity.isPushable()) {
                return false;
            }

            if (currentEntityIsClient) {
                if (otherEntity instanceof PlayerEntity playerEntity) {
                    if (!playerEntity.isMainPlayer()) {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            AbstractTeam otherTeam = otherEntity.getScoreboardTeam();
            if (otherTeam == null) {
                return currentEntityNoPushOwnTeam && currentEntityNoPushOtherTeams;
            } else {
                AbstractTeam.CollisionRule otherCollisionRule = otherTeam.getCollisionRule();
                if (otherTeam.isEqual(currentTeam)) {
                    boolean otherEntityNoPushOwnTeam = otherCollisionRule != AbstractTeam.CollisionRule.PUSH_OWN_TEAM;
                    return currentEntityNoPushOwnTeam && otherEntityNoPushOwnTeam;
                } else {
                    boolean otherEntityNoPushOtherTeams = otherCollisionRule != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS;

                    return currentEntityNoPushOtherTeams && otherEntityNoPushOtherTeams;
                }
            }
        };
    }
}
