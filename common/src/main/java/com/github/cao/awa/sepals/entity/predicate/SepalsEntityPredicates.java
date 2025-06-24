package com.github.cao.awa.sepals.entity.predicate;

import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
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

        boolean currentEntityPushOwnTeam = currentCollisionRule == AbstractTeam.CollisionRule.PUSH_OWN_TEAM;
        boolean currentEntityNoPushOwnTeams = !currentEntityPushOwnTeam;
        boolean currentEntityNoPushOtherTeams = currentCollisionRule != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS;

        boolean currentEntityIsClient = currentEntity.getWorld().isClient;

        return EntityPredicates.EXCEPT_SPECTATOR.and((otherEntity) -> {
            if (otherEntity.isPushable()) {
                if (currentEntityIsClient) {
                    label62:
                    {
                        if (otherEntity instanceof PlayerEntity playerEntity) {
                            if (playerEntity.isMainPlayer()) {
                                break label62;
                            }
                        }

                        return false;
                    }
                }

                AbstractTeam otherTeam = otherEntity.getScoreboardTeam();
                if (otherTeam == null) {
                    if (currentEntityPushOwnTeam) {
                        return false;
                    } else {
                        return currentEntityNoPushOtherTeams;
                    }
                } else {
                    assert currentTeam != null;
                    boolean isTeamEqual = currentTeam.isEqual(otherTeam);
                    AbstractTeam.CollisionRule otherCollisionRule = otherTeam.getCollisionRule();
                    boolean otherEntityNoPushOwnTeam = otherCollisionRule != AbstractTeam.CollisionRule.PUSH_OWN_TEAM;
                    if (isTeamEqual) {
                        return currentEntityNoPushOwnTeams && otherEntityNoPushOwnTeam;
                    } else {
                        boolean otherEntityNoPushOtherTeams = otherCollisionRule != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS;

                        return currentEntityNoPushOtherTeams && otherEntityNoPushOtherTeams;
                    }
                }
            } else {
                return false;
            }
        });
    }
}
