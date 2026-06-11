package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class GooseMigrationBehavior extends Behavior<Goose> {
    private static final double DESPAWN_RANGE = 128.0;
    private static final int MIN_AIRBORNE_TICKS = 100;
    private static final float SPIRAL_CLIMB_TURN = 2.5F;
    private static final float CLIMB_SPEED = 0.3F;
    private static final float CLIMB_LIFT = 0.18F;
    private static final float CRUISE_SPEED = 0.55F;
    private static final double CRUISE_LIFT = 0.02;
    private static final float CRUISE_TURN_RATE = 3.0F;
    private static final int CRUISE_CLEARANCE = 8;
    private static final float APPROACH_SPEED = 0.45F;
    private static final float APPROACH_TURN_RATE = 6.0F;
    private static final double APPROACH_GLIDE_SLOPE = 0.35;
    private static final int APPROACH_CLEARANCE = 4;
    private static final float LANDING_SPEED = 0.26F;
    private static final float LANDING_TURN_RATE = 6.0F;
    private static final double LANDING_DISTANCE = 10.0;
    private static final double SPIRAL_RADIUS_SQR = 4.0;
    private static final double FOLLOW_GAIN = 0.08;
    private static final double FOLLOW_MAX_SPEED = 0.8;
    private static final int MAX_BLOCKED_TICKS = 60;
    private static final int MAX_ARRIVAL_TICKS = 1500;
    private static final double CONTINUE_DISTANCE_SQR = 9.0;
    private static final float CONTINUE_SPEED = 1.1F;

    private int airborneTicks;
    private int blockedTicks;
    private float flightYaw;

    public GooseMigrationBehavior() {
        super(Map.of(), Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        return goose.isMigrating();
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        airborneTicks = 0;
        blockedTicks = 0;
        flightYaw = goose.getYRot();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return goose.isMigrating();
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (goose.isLeashed() || goose.isPassenger()) {
            goose.finishMigrationFlight();
            return;
        }
        Brain<Goose> brain = goose.getBrain();
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        goose.setFlying(true);
        goose.resetFallDistance();
        airborneTicks++;

        if (goose.isArriving()) {
            tickArrival(level, goose);
        } else {
            tickDeparture(level, goose);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        if (goose.isMigrating()) goose.finishMigrationFlight();
    }

    private void tickDeparture(ServerLevel level, Goose goose) {
        Goose leader = goose.getFlockLeader();
        if (leader != null && leader.isAlive() && leader.isMigrating() && !leader.isArriving()) {
            followLeader(goose, leader);
        } else {
            leadDeparture(level, goose);
        }

        if (airborneTicks > MIN_AIRBORNE_TICKS
                && !level.hasNearbyAlivePlayer(goose.getX(), goose.getY(), goose.getZ(), DESPAWN_RANGE)) {
            GooseMigration.get(level).recordDeparted();
            goose.discard();
        }
    }

    private void leadDeparture(ServerLevel level, Goose goose) {
        Vec3 heading = goose.getMigrationHeading();
        if (heading == null) {
            goose.finishMigrationFlight();
            return;
        }

        if (goose.getY() < goose.getMigrationCeiling()) {
            flightYaw += SPIRAL_CLIMB_TURN;
            Vec3 direction = yawDirection();
            goose.setDeltaMovement(direction.scale(CLIMB_SPEED).add(0, CLIMB_LIFT, 0));
            if (goose.verticalCollision) {
                if (++blockedTicks > MAX_BLOCKED_TICKS) {
                    goose.finishMigrationFlight();
                    return;
                }
            } else if (blockedTicks > 0) {
                blockedTicks--;
            }
        } else {
            flightYaw = Mth.approachDegrees(flightYaw, yawOf(heading), CRUISE_TURN_RATE);
            Vec3 direction = yawDirection();
            double clearanceY = clearanceAhead(level, goose, direction, 16, 32) + CRUISE_CLEARANCE;
            double lift = goose.getY() < clearanceY ? CLIMB_LIFT : CRUISE_LIFT;
            if (goose.horizontalCollision) lift = 0.25;
            goose.setDeltaMovement(direction.scale(CRUISE_SPEED).add(0, lift, 0));
        }
        applyRotation(goose);
    }

    private void tickArrival(ServerLevel level, Goose goose) {
        if (airborneTicks > MAX_ARRIVAL_TICKS) {
            goose.finishMigrationFlight();
            return;
        }

        Goose leader = goose.getFlockLeader();
        if (leader != null && leader.isAlive() && leader.isMigrating()) {
            followLeader(goose, leader);
            return;
        }

        BlockPos landing = goose.getLandingSpot();
        if (landing == null) {
            goose.finishMigrationFlight();
            return;
        }

        Vec3 target = Vec3.atBottomCenterOf(landing);
        double horizontalDistance = Math.sqrt(horizontalDistanceSqr(goose, target));

        if (horizontalDistance > LANDING_DISTANCE) {
            turnToward(goose, target, APPROACH_TURN_RATE);
            Vec3 direction = yawDirection();
            double clearanceY = clearanceAhead(level, goose, direction, 8, 16) + APPROACH_CLEARANCE;
            double glideY = landing.getY() + horizontalDistance * APPROACH_GLIDE_SLOPE;
            double desiredY = Math.max(clearanceY, glideY);
            double lift = Mth.clamp((desiredY - goose.getY()) * 0.1, -0.16, 0.1);
            goose.setDeltaMovement(direction.scale(APPROACH_SPEED).add(0, lift, 0));
        } else {
            if (horizontalDistanceSqr(goose, target) < SPIRAL_RADIUS_SQR) {
                flightYaw += LANDING_TURN_RATE;
            } else {
                turnToward(goose, target, LANDING_TURN_RATE);
            }
            Vec3 direction = yawDirection();
            double sink = Mth.clamp((landing.getY() - goose.getY()) * 0.1, -0.12, 0.02);
            if (goose.getY() > landing.getY() + 1) sink = Math.min(sink, -0.05);
            goose.setDeltaMovement(direction.scale(LANDING_SPEED).add(0, sink, 0));
        }
        applyRotation(goose);

        if (goose.onGround() || goose.isInWater()) {
            goose.finishMigrationFlight();
            goose.honk();
            if (landing.distSqr(goose.blockPosition()) > CONTINUE_DISTANCE_SQR) {
                goose.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                        new WalkTarget(landing, CONTINUE_SPEED, 1));
            }
        }
    }

    private void followLeader(Goose goose, Goose leader) {
        Vec3 leaderVelocity = leader.getDeltaMovement();
        Vec3 heading = horizontalOrFallback(leaderVelocity, goose);
        Vec3 target = leader.position().add(formationOffset(goose.getFormationIndex(), heading));
        Vec3 desired = target.subtract(goose.position());
        Vec3 delta = leaderVelocity.add(desired.scale(FOLLOW_GAIN));
        if (delta.length() > FOLLOW_MAX_SPEED) delta = delta.normalize().scale(FOLLOW_MAX_SPEED);
        goose.setDeltaMovement(delta);
        flightYaw = yawOf(heading);
        applyRotation(goose);
    }

    static Vec3 formationOffset(int index, Vec3 heading) {
        int rank = (index + 1) / 2;
        double side = index % 2 == 1 ? 1 : -1;
        Vec3 lateral = new Vec3(-heading.z, 0, heading.x).scale(2.4 * rank * side);
        return heading.scale(-2.8 * rank).add(lateral).add(0, 0.3 * rank, 0);
    }

    private void turnToward(Goose goose, Vec3 target, float turnRate) {
        double dx = target.x - goose.getX();
        double dz = target.z - goose.getZ();
        if (dx * dx + dz * dz < 0.01) return;
        float desiredYaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        flightYaw = Mth.approachDegrees(flightYaw, desiredYaw, turnRate);
    }

    private Vec3 yawDirection() {
        float radians = flightYaw * ((float) Math.PI / 180F);
        return new Vec3(-Mth.sin(radians), 0, Mth.cos(radians));
    }

    private void applyRotation(Goose goose) {
        goose.setYRot(flightYaw);
        goose.yBodyRot = flightYaw;
        goose.yHeadRot = flightYaw;
    }

    private static double clearanceAhead(ServerLevel level, Goose goose, Vec3 direction, int near, int far) {
        int here = level.getHeight(Heightmap.Types.MOTION_BLOCKING, goose.getBlockX(), goose.getBlockZ());
        int ahead = level.getHeight(Heightmap.Types.MOTION_BLOCKING,
                Mth.floor(goose.getX() + direction.x * near), Mth.floor(goose.getZ() + direction.z * near));
        int beyond = level.getHeight(Heightmap.Types.MOTION_BLOCKING,
                Mth.floor(goose.getX() + direction.x * far), Mth.floor(goose.getZ() + direction.z * far));
        return Math.max(here, Math.max(ahead, beyond));
    }

    private static double horizontalDistanceSqr(Goose goose, Vec3 target) {
        double dx = target.x - goose.getX();
        double dz = target.z - goose.getZ();
        return dx * dx + dz * dz;
    }

    private static Vec3 horizontalOrFallback(Vec3 velocity, Goose goose) {
        Vec3 flat = new Vec3(velocity.x, 0, velocity.z);
        if (flat.lengthSqr() > 1.0E-4) return flat.normalize();
        Vec3 stored = goose.getMigrationHeading();
        if (stored != null) return stored;
        float radians = goose.getYRot() * ((float) Math.PI / 180F);
        return new Vec3(-Mth.sin(radians), 0, Mth.cos(radians));
    }

    private static float yawOf(Vec3 heading) {
        return (float) (Mth.atan2(heading.z, heading.x) * (180.0 / Math.PI)) - 90.0F;
    }
}
