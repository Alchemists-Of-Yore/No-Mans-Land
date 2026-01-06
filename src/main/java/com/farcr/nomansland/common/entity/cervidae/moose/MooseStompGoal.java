package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ai.targeting.*;
import net.minecraft.world.entity.ai.util.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;

import javax.annotation.*;
import java.util.*;


/**
 * Behavior similar to {@link net.minecraft.world.entity.ai.goal.AvoidEntityGoal}
 * Avoids specified entities from a certain radius. Occasionally Stomps if any are found within a certain radius.
 */
public class MooseStompGoal extends Goal {

    private static final TargetingConditions INTROVERT_TARGETING = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight();

    protected final Moose moose;
    protected final double speedModifier;
    protected final float stompDistance;
    protected final PathNavigation pathNav;

    @Nullable
    protected Path path;
    @Nullable
    protected LivingEntity avoidedTarget;

    public MooseStompGoal(Moose moose, double speedModifier, float stompDistance) {
        this.moose = moose;
        this.speedModifier = speedModifier;
        this.stompDistance = stompDistance;
        this.pathNav = moose.getNavigation();
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    public boolean shouldStomp(Entity entity) {
        if (!moose.isPacified()) {
            if (entity instanceof Player) {
                return true;
            }
        }
        return entity instanceof Monster;
    }

    @Override
    public boolean canUse() {
        if (!moose.canStomp()) {
            return false;
        }
        var level = moose.level();
        var stompArea = moose.getBoundingBox().inflate(stompDistance, 3.0, stompDistance);
        var stompOnSight = level
                .getEntitiesOfClass(LivingEntity.class, stompArea,
                        EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(this::shouldStomp));

        avoidedTarget = level.getNearestEntity(
                stompOnSight, INTROVERT_TARGETING,
                moose, moose.getX(), moose.getY(), moose.getZ());

        if (avoidedTarget == null) {
            return false;
        }
        if (moose.distanceTo(avoidedTarget) > stompDistance) {
            return false;
        }
        Vec3 escapePos = DefaultRandomPos.getPosAway(moose, 4, 2, avoidedTarget.position());
        if (escapePos == null) {
            return false;
        }
        path = pathNav.createPath(escapePos.x, escapePos.y, escapePos.z, 0);
        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !pathNav.isDone() || moose.stompStateTimer > 0;
    }

    @Override
    public void start() {
        pathNav.moveTo(path, speedModifier);
        if (avoidedTarget != null) {
            moose.level().broadcastEntityEvent(moose, Moose.START_STOMP_EVENT);
            moose.isInStompState = true;
        }
    }

    @Override
    public void stop() {
        avoidedTarget = null;
    }

    @Override
    public void tick() {
        moose.getNavigation().setSpeedModifier(moose.getStompAdjustedMovementSpeed((float) speedModifier));
    }
}