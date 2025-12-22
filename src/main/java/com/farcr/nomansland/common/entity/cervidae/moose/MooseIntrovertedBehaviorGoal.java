package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;


/**
 * Behavior similar to {@link net.minecraft.world.entity.ai.goal.AvoidEntityGoal}
 * Avoids specified entities from a certain radius. Occasionally Stomps if any are found within a certain radius.
 */
public class MooseIntrovertedBehaviorGoal extends Goal {

    private static final TargetingConditions INTROVERT_TARGETING = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight();

    protected final Moose moose;
    protected final double speedModifier;
    protected final float introvertDistance;
    protected final float stompDistance;
    protected final PathNavigation pathNav;

    @Nullable
    protected Path path;
    @Nullable
    protected LivingEntity avoidedTarget;

    public MooseIntrovertedBehaviorGoal(Moose moose, double speedModifier, float introvertDistance, float stompDistance) {
        this.moose = moose;
        this.speedModifier = speedModifier;
        this.introvertDistance = introvertDistance;
        this.stompDistance = stompDistance;
        this.pathNav = moose.getNavigation();
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    public boolean shouldPassivelyAvoid(Entity entity) {
        return !moose.isPacified() && entity instanceof Player;
    }

    public boolean shouldStompButNotAvoid(Entity entity) {
        return entity instanceof Monster;
    }

    @Override
    public boolean canUse() {
        var introvertArea = moose.getBoundingBox().inflate(introvertDistance, 3.0, introvertDistance);
        var avoided = moose.level()
                .getEntitiesOfClass(LivingEntity.class, introvertArea,
                        EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(this::shouldPassivelyAvoid));

        var stompArea = moose.getBoundingBox().inflate(stompDistance, 3.0, stompDistance);
        var stompOnSight = moose.level()
                .getEntitiesOfClass(LivingEntity.class, stompArea,
                        EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(this::shouldStompButNotAvoid));

        var relevantList = stompOnSight.isEmpty() ? avoided : stompOnSight;
        avoidedTarget = moose
                .level()
                .getNearestEntity(
                        relevantList, INTROVERT_TARGETING,
                        moose, moose.getX(), moose.getY(), moose.getZ());

        if (avoidedTarget == null) {
            return false;
        }
        Vec3 escapePos = DefaultRandomPos.getPosAway(moose, 8, 4, avoidedTarget.position());
        if (escapePos == null) {
            return false;
        }
        if (avoidedTarget.distanceToSqr(escapePos.x, escapePos.y, escapePos.z) < avoidedTarget.distanceToSqr(moose)) {
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
    }

    @Override
    public void stop() {
        avoidedTarget = null;
    }

    @Override
    public void tick() {
        if (moose.canStomp()) {
            if (avoidedTarget != null && moose.distanceTo(avoidedTarget) < stompDistance) {
                moose.level().broadcastEntityEvent(moose, Moose.START_STOMP_EVENT);
                moose.isInStompState = true;
            }
        }

        moose.getNavigation().setSpeedModifier(moose.getStompAdjustedMovementSpeed((float) speedModifier));
    }

}