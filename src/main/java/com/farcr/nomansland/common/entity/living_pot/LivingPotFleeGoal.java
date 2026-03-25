package com.farcr.nomansland.common.entity.living_pot;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class LivingPotFleeGoal extends Goal {

    private static final double SEARCH_RADIUS = 20.0;
    private static final double BEHIND_DISTANCE = 2.5;
    private static final double ARRIVE_DIST_SQ = 2.0 * 2.0;
    private static final int REPATH_INTERVAL = 10;

    private final LivingPot pot;
    private final double speed;
    @Nullable private LivingPot allyPot;
    private int repathTick;

    public LivingPotFleeGoal(LivingPot pot, double speed) {
        this.pot = pot;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return pot.isSmall() && pot.isAngry() && (canSeeAngerTarget() || pot.getLastKnownTargetPos() != null);
    }

    @Override
    public boolean canContinueToUse() {
        return pot.isSmall() && pot.isAngry() && (canSeeAngerTarget() || pot.getLastKnownTargetPos() != null);
    }

    private boolean canSeeAngerTarget() {
        Player target = pot.getPersistentAngerTarget() != null
                ? pot.level().getPlayerByUUID(pot.getPersistentAngerTarget())
                : null;
        return target != null && target.canBeSeenAsEnemy() && !target.isInvisible();
    }

    @Override
    public void start() {
        repathTick = 0;
        allyPot = findNearestLargeAlly();
    }

    @Override
    public void tick() {
        repathTick++;

        if (repathTick % REPATH_INTERVAL == 0) {
            allyPot = findNearestLargeAlly();
        }

        Player player = pot.getPersistentAngerTarget() != null
                ? pot.level().getPlayerByUUID(pot.getPersistentAngerTarget())
                : null;

        Vec3 threatPos;
        if (player != null && !player.isInvisible()) {
            threatPos = player.position();
        } else if (pot.getLastKnownTargetPos() != null) {
            threatPos = pot.getLastKnownTargetPos();
        } else {
            return;
        }

        if (allyPot != null && allyPot.isAlive()) {
            Vec3 playerToAlly = allyPot.position().subtract(threatPos).normalize();
            Vec3 hidePos = allyPot.position().add(playerToAlly.scale(BEHIND_DISTANCE));
            pot.getNavigation().moveTo(hidePos.x, hidePos.y, hidePos.z, speed);
        } else {
            Vec3 fleeDir = pot.position().subtract(threatPos).normalize();
            Vec3 fleeTarget = pot.position().add(fleeDir.scale(8));
            Vec3 pos = DefaultRandomPos.getPosTowards(pot, 10, 7, fleeTarget, Math.PI / 3);
            if (pos != null) {
                pot.getNavigation().moveTo(pos.x, pos.y, pos.z, speed);
            }
        }
    }

    @Override
    public void stop() {
        allyPot = null;
        pot.getNavigation().stop();
    }

    @Nullable
    private LivingPot findNearestLargeAlly() {
        AABB searchBox = pot.getBoundingBox().inflate(SEARCH_RADIUS);
        List<LivingPot> allies = pot.level().getEntitiesOfClass(LivingPot.class, searchBox,
                other -> other != pot
                        && other.isAlive()
                        && other.isLarge());

        LivingPot nearest = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingPot ally : allies) {
            double d = pot.distanceToSqr(ally);
            if (d < bestDist) {
                bestDist = d;
                nearest = ally;
            }
        }
        return nearest;
    }
}
