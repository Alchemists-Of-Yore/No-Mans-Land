package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ai.targeting.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.pathfinder.*;

import javax.annotation.*;
import java.util.*;


/**
 * Behavior similar to {@link net.minecraft.world.entity.ai.goal.AvoidEntityGoal}
 * Avoids specified entities from a certain radius. Occasionally Stomps if any are found within a certain radius.
 */
public class MooseStompGoal extends Goal {

    private static final TargetingConditions STOMP_TARGETING = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight();

    protected final Moose moose;
    protected final float stompDistance;

    @Nullable
    protected LivingEntity stompTarget;

    public MooseStompGoal(Moose moose, float stompDistance) {
        this.moose = moose;
        this.stompDistance = stompDistance;
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
        if (!moose.canStartStomp()) {
            return false;
        }
        var level = moose.level();
        var stompArea = moose.getBoundingBox().inflate(stompDistance, 3.0, stompDistance);
        var stompOnSight = level
                .getEntitiesOfClass(LivingEntity.class, stompArea,
                        EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(this::shouldStomp));

        stompTarget = level.getNearestEntity(
                stompOnSight, STOMP_TARGETING,
                moose, moose.getX(), moose.getY(), moose.getZ());

        if (stompTarget == null) {
            return false;
        }
        return (moose.distanceTo(stompTarget) < stompDistance);
    }

    @Override
    public boolean canContinueToUse() {
        return moose.isStomping;
    }

    @Override
    public void start() {
        moose.startStomping();
    }

    @Override
    public void stop() {
        stompTarget = null;
    }
}