package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.EnumSet;


/**
 * Behavior similar to {@link net.minecraft.world.entity.ai.goal.AvoidEntityGoal}
 * Avoids specified entities from a certain radius. Occasionally Stomps if any are found within a certain radius.
 */
public class MooseStompGoal extends Goal {

    private static final TargetingConditions STOMP_TARGETING = TargetingConditions.forNonCombat().range(Moose.STOMP_DISTANCE * 1.5f);

    protected final Moose moose;
    protected final float stompDistance;
    protected int inStompRadius;

    @Nullable
    protected LivingEntity stompTarget;

    public MooseStompGoal(Moose moose, float stompDistance) {
        this.moose = moose;
        this.stompDistance = stompDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    public boolean shouldStomp(Entity entity) {
        if (moose.targetMemory.isUpsetAt(entity)) {
            return false;
        }
        if (!moose.isPacified()) {
            if (entity instanceof Player) {
                return true;
            }
        }
        return entity instanceof Monster;
    }

    @Override
    public boolean canUse() {
        var level = moose.level();
        if (!moose.canStartStomp()) {
            return false;
        }
        var stompArea = moose.getBoundingBox().inflate(stompDistance, 3.0, stompDistance);
        var stompTargets = level
                .getEntitiesOfClass(LivingEntity.class, stompArea,
                        EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(this::shouldStomp));

        stompTarget = level.getNearestEntity(
                stompTargets, STOMP_TARGETING,
                moose, moose.getX(), moose.getY(), moose.getZ());

        if (stompTarget == null) {
            if (inStompRadius > 0) {
                inStompRadius--;
            }
            return false;
        }
        if (moose.distanceTo(stompTarget) < stompDistance) {
            inStompRadius++;
        }
        return inStompRadius > Moose.STOMP_WINDUP;
    }

    @Override
    public boolean canContinueToUse() {
        return moose.isStomping;
    }

    @Override
    public void start() {
        moose.startStomping();
        moose.getNavigation().stop();
        inStompRadius = 0;
    }

    @Override
    public void stop() {
        stompTarget = null;
    }
}