package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GooseVoluntaryFlightBehavior extends Behavior<Goose> {
    private enum Phase { TAKEOFF, CRUISE, LAND }

    private static final double MIN_TRAVEL = 8.0;
    private static final int WATER_RADIUS = 28;
    private static final int WATER_STEP = 2;

    private static final float CRUISE_SPEED = 0.55F;
    private static final float TURN_RATE = 7.0F;
    private static final float ACCEL = 0.12F;
    private static final double CLIMB_CAP = 0.36;
    private static final double LAND_CLIMB_CAP = 0.24;
    private static final double DESCENT_CAP = 0.45;
    private static final double FLY_HEIGHT = 5.0;

    private Phase phase = Phase.TAKEOFF;
    @Nullable
    private Vec3 destination;
    private double takeoffY;
    private int phaseTicks;
    private int stuckTicks;
    private boolean liftedOff;
    private boolean groupFlight;
    private long nextFlightTime;

    public GooseVoluntaryFlightBehavior() {
        super(Map.of(), Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isBaby() || goose.isFlying() || goose.isMigrating() || goose.isPassenger()
                || goose.isLeashed() || goose.isInLove() || goose.isCarrying() || goose.isStealing()) return false;
        if (!goose.onGround()) return false;
        Brain<Goose> brain = goose.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                || brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)
                || brain.hasMemoryValue(MemoryModuleType.BREED_TARGET)) return false;

        groupFlight = false;
        Vec3 invited = goose.pendingFlightInvite();
        if (invited != null) {
            goose.clearFlightInvite();
            destination = invited;
            return true;
        }

        if (level.getGameTime() < nextFlightTime) return false;
        if (!level.canSeeSky(goose.blockPosition().above(2))) return false;

        destination = crossingDestination(level, goose, brain);
        if (destination == null) {
            destination = relocationDestination(level, goose, brain);
            groupFlight = destination != null;
        }
        return destination != null;
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        Brain<Goose> brain = goose.getBrain();
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.PATH);
        goose.getNavigation().stop();
        goose.setFlying(true);
        goose.setDeltaMovement(goose.getDeltaMovement().add(0, 0.28, 0));
        goose.honk();
        phase = Phase.TAKEOFF;
        phaseTicks = 0;
        stuckTicks = 0;
        liftedOff = false;
        takeoffY = goose.getY();
        if (groupFlight && destination != null) inviteFlock(goose);
    }

    private void inviteFlock(Goose goose) {
        for (Goose other : goose.nearbyGeese(10.0)) {
            if (other.isBaby() || other.isFlying() || other.isMigrating() || other.isPassenger()
                    || other.isLeashed() || other.isInLove() || other.isCarrying() || other.isStealing()
                    || !other.onGround()) continue;
            Brain<Goose> otherBrain = other.getBrain();
            if (otherBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                    || otherBrain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)
                    || otherBrain.hasMemoryValue(MemoryModuleType.BREED_TARGET)) continue;
            double offsetX = (goose.getRandom().nextDouble() - 0.5) * 5.0;
            double offsetZ = (goose.getRandom().nextDouble() - 0.5) * 5.0;
            other.inviteFlight(destination.add(offsetX, 0, offsetZ));
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        Brain<Goose> brain = goose.getBrain();
        return goose.isFlying() && !goose.isMigrating() && !goose.isPassenger() && !goose.isLeashed()
                && !brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && !brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET);
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        Brain<Goose> brain = goose.getBrain();
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        goose.markFlightControl();
        goose.resetFallDistance();
        phaseTicks++;

        if (!goose.onGround()) liftedOff = true;
        if (liftedOff && (goose.onGround() || goose.isInWater())) {
            goose.setFlying(false);
            return;
        }
        if (destination == null) {
            phase = Phase.LAND;
        }
        switch (phase) {
            case TAKEOFF -> takeoff(goose);
            case CRUISE -> cruise(level, goose);
            case LAND -> land(goose);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        goose.setFlying(false);
        nextFlightTime = gameTime + 600 + goose.getRandom().nextInt(1200);
        if (destination != null && horizontalDistanceSqr(goose, destination) > 9.0
                && !goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            goose.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                    new WalkTarget(BlockPos.containing(destination), 1.0F, 1));
        }
        destination = null;
    }

    private void takeoff(Goose goose) {
        GooseFlight.towardPoint(goose, destination.x, destination.z, takeoffY + FLY_HEIGHT, 0.34F, TURN_RATE, ACCEL, CLIMB_CAP);
        if (!liftedOff && phaseTicks > 10) {
            goose.setFlying(false);
            return;
        }
        if (goose.getY() >= takeoffY + 3.0 || phaseTicks > 30) {
            enterPhase(Phase.CRUISE);
        }
    }

    private void cruise(ServerLevel level, Goose goose) {
        double dx = destination.x - goose.getX();
        double dz = destination.z - goose.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        double dirX = dist > 1.0E-4 ? dx / dist : 0;
        double dirZ = dist > 1.0E-4 ? dz / dist : 0;
        double terrain = GooseFlight.terrainAhead(level, goose.getX(), goose.getZ(), dirX, dirZ, 6, 12);
        GooseFlight.towardPoint(goose, destination.x, destination.z, terrain + FLY_HEIGHT, CRUISE_SPEED, TURN_RATE, ACCEL, CLIMB_CAP);
        if (goose.horizontalCollision && ++stuckTicks > 8) {
            enterPhase(Phase.LAND);
            return;
        }
        if (!goose.horizontalCollision) stuckTicks = 0;
        if (horizontalDistanceSqr(goose, destination) < 49.0 || phaseTicks > 160) {
            enterPhase(Phase.LAND);
        }
    }

    private void land(Goose goose) {
        Vec3 target = destination != null ? destination : goose.position();
        double groundY = target.y;
        double above = goose.getY() - groundY;
        double dist = Math.sqrt(horizontalDistanceSqr(goose, target));
        if (above <= 0.6 || (dist < 1.2 && above <= 1.4)) {
            goose.flapBriefly();
            goose.setFlying(false);
            return;
        }
        if (dist < 2.5) {
            GooseFlight.descend(goose, 0.75, 0.08, DESCENT_CAP);
        } else {
            double targetY = groundY + Math.min(dist * 0.45, FLY_HEIGHT);
            float speed = (float) Mth.clamp(dist * 0.12, 0.15, CRUISE_SPEED);
            GooseFlight.approach(goose, target.x, target.z, targetY, speed, TURN_RATE, ACCEL, LAND_CLIMB_CAP, DESCENT_CAP);
        }
        if (phaseTicks > 90) {
            goose.setFlying(false);
        }
    }

    private void enterPhase(Phase next) {
        phase = next;
        phaseTicks = 0;
        stuckTicks = 0;
    }

    @Nullable
    private static Vec3 crossingDestination(ServerLevel level, Goose goose, Brain<Goose> brain) {
        if (!brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) return null;
        long stuckSince = brain.getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE).orElse(Long.MAX_VALUE);
        if (level.getGameTime() - stuckSince < 40L) return null;
        WalkTarget walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        if (walkTarget == null) return null;
        BlockPos pos = walkTarget.getTarget().currentBlockPosition();
        double horizontal = goose.distanceToSqr(pos.getX() + 0.5, goose.getY(), pos.getZ() + 0.5);
        if (horizontal < MIN_TRAVEL * MIN_TRAVEL || horizontal > 36.0 * 36.0) return null;
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        return new Vec3(pos.getX() + 0.5, surfaceY, pos.getZ() + 0.5);
    }

    @Nullable
    private static Vec3 relocationDestination(ServerLevel level, Goose goose, Brain<Goose> brain) {
        if (goose.isInWater() || brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) return null;
        if (goose.getRandom().nextInt(120) != 0) return null;

        BlockPos origin = goose.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -WATER_RADIUS; dx <= WATER_RADIUS; dx += WATER_STEP) {
            for (int dz = -WATER_RADIUS; dz <= WATER_RADIUS; dz += WATER_STEP) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                if (!level.isLoaded(new BlockPos(x, level.getMinBuildHeight(), z))) continue;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos surface = new BlockPos(x, surfaceY - 1, z);
                if (!level.getFluidState(surface).is(FluidTags.WATER)) continue;
                double distance = surface.distSqr(origin);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = surface;
                }
            }
        }
        if (best == null || bestDistance < 144.0) return null;
        return new Vec3(best.getX() + 0.5, best.getY() + 1, best.getZ() + 0.5);
    }

    private static double horizontalDistanceSqr(Goose goose, Vec3 target) {
        double dx = target.x - goose.getX();
        double dz = target.z - goose.getZ();
        return dx * dx + dz * dz;
    }
}
