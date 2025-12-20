package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;


/**
 * Behavior similar to {@link net.minecraft.world.entity.ai.goal.AvoidEntityGoal}
 * Avoids specified entities from a certain radius. Occasionally Stomps if any are found within a certain radius.
 */
public class MooseIntrovertedBehaviorGoal extends Goal {

    private static final TargetingConditions ESCAPE_TARGETING = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight();

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

    public boolean shouldAvoid(Entity entity) {
        //TODO: Replace this with a tag
        return entity instanceof Player || entity instanceof Wolf || (entity instanceof Mob mob && mob.isAggressive());
    }

    @Override
    public boolean canUse() {
        List<LivingEntity> nearbyEntities = moose.level()
                .getEntitiesOfClass(LivingEntity.class, moose.getBoundingBox().inflate(introvertDistance, 3.0, introvertDistance),
                        EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(this::shouldAvoid));
        avoidedTarget = moose
                .level()
                .getNearestEntity(
                        nearbyEntities, ESCAPE_TARGETING,
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
    }

    @Override
    public void stop() {
        avoidedTarget = null;
    }

    @Override
    public void tick() {
        moose.getNavigation().setSpeedModifier(speedModifier);
        if (avoidedTarget != null && moose.distanceTo(avoidedTarget) < stompDistance) {
            avoidedTarget.igniteForTicks(10);
        }
    }
}