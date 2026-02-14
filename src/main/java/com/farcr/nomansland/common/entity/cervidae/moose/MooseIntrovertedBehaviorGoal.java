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
import net.minecraft.world.level.*;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;


/**
 * Behavior similar to {@link net.minecraft.world.entity.ai.goal.AvoidEntityGoal}
 * Avoids specified entities from a certain radius. Moves faster after stomping.
 */
public class MooseIntrovertedBehaviorGoal extends Goal {

    private static final TargetingConditions INTROVERT_TARGETING = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight();

    protected final Moose moose;
    protected final double speedModifier;
    protected final float introvertDistance;
    protected final PathNavigation pathNav;

    @Nullable
    protected Path path;

    public MooseIntrovertedBehaviorGoal(Moose moose, double speedModifier, float introvertDistance) {
        this.moose = moose;
        this.speedModifier = speedModifier;
        this.introvertDistance = introvertDistance;
        this.pathNav = moose.getNavigation();
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    public boolean shouldPassivelyAvoid(Entity entity) {
        return !moose.isPacified() && entity instanceof Player;
    }

    @Override
    public boolean canUse() {
        if (moose.isVehicle()) {
            return false;
        }
        var introvertArea = moose.getBoundingBox().inflate(introvertDistance, 3.0, introvertDistance);
        var level = moose
                .level();
        var avoided = level.getEntitiesOfClass(LivingEntity.class, introvertArea, EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(this::shouldPassivelyAvoid));

        var avoidedTarget = level.getNearestEntity(
                avoided, INTROVERT_TARGETING,
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
        return !pathNav.isDone();
    }

    @Override
    public void start() {
        pathNav.moveTo(path, speedModifier);
        pathNav.setSpeedModifier(speedModifier);
    }

    @Override
    public void tick() {
        moose.getNavigation().setSpeedModifier(moose.getStompAdjustedMovementSpeed((float) speedModifier));
    }
}