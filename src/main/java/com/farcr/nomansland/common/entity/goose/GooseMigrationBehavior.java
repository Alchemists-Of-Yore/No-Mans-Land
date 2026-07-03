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
    private static final float APPROACH_SPEED = 0.5F;
    private static final float LANDING_TURN_RATE = 11.0F;
    private static final float ACCEL = 0.11F;
    private static final double CLIMB_CAP = 0.4;
    private static final double LAND_CLIMB_CAP = 0.22;
    private static final double DESCENT_CAP = 0.5;
    private static final double FOLLOW_MAX_SPEED = 0.85;

    private int airborneTicks;
    private int blockedTicks;
    private int honkTimer;

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
        honkTimer = goose.getRandom().nextInt(40);
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

        if (--honkTimer <= 0) {
            goose.honk();
            honkTimer = 40 + goose.getRandom().nextInt(60);
        }

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

        if (airborneTicks > 100
                && !level.hasNearbyAlivePlayer(goose.getX(), goose.getY(), goose.getZ(), 128.0)) {
            if (!goose.isTransit()) GooseMigration.get(level).recordDeparted();
            goose.discard();
        }
    }

    private void leadDeparture(ServerLevel level, Goose goose) {
        Vec3 heading = goose.getMigrationHeading();
        if (heading == null) {
            goose.finishMigrationFlight();
            return;
        }

        double ceiling = goose.getMigrationCeiling();
        if (goose.getY() < ceiling - 2) {
            GooseFlight.spiral(goose, 7.0F, ceiling + 4, 0.4F, ACCEL, CLIMB_CAP);
            if (goose.verticalCollision) {
                if (++blockedTicks > 60) {
                    goose.finishMigrationFlight();
                }
            } else if (blockedTicks > 0) {
                blockedTicks--;
            }
        } else {
            double targetY = Math.max(ceiling, terrainAhead(level, goose, heading) + 13);
            GooseFlight.alongHeading(goose, heading, targetY, 0.62F, 4.0F, ACCEL, CLIMB_CAP);
        }
    }

    private void tickArrival(ServerLevel level, Goose goose) {
        if (airborneTicks > 1500) {
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
        double horizontal = Math.sqrt(horizontalDistanceSqr(goose, target));
        double groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, goose.getBlockX(), goose.getBlockZ());
        double above = goose.getY() - groundY;

        if (goose.onGround() || goose.isInWater() || above <= 0.6 || (horizontal < 1.2 && goose.getY() - landing.getY() <= 1.4)) {
            goose.flapBriefly();
            goose.finishMigrationFlight();
            goose.honk();
            if (landing.distSqr(goose.blockPosition()) > 9.0) {
                goose.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                        new WalkTarget(landing, 1.1F, 1));
            }
            return;
        }

        if (horizontal > 12.0) {
            double glideY = landing.getY() + horizontal * 0.4;
            double targetY = Math.max(glideY, terrainAhead(level, goose, headingVec(goose)) + 6);
            GooseFlight.towardPoint(goose, target.x, target.z, targetY, APPROACH_SPEED, 6.0F, ACCEL, CLIMB_CAP);
        } else if (horizontal > 2.5) {
            double targetY = landing.getY() + horizontal * 0.6;
            float speed = (float) Mth.clamp(horizontal * 0.09, 0.17, APPROACH_SPEED);
            GooseFlight.approach(goose, target.x, target.z, targetY, speed, LANDING_TURN_RATE, ACCEL, LAND_CLIMB_CAP, DESCENT_CAP);
        } else {
            GooseFlight.descend(goose, 0.75, 0.08, DESCENT_CAP);
        }
    }

    private void followLeader(Goose goose, Goose leader) {
        Vec3 leaderVelocity = leader.getDeltaMovement();
        Vec3 heading = horizontalOrFallback(leaderVelocity, goose);
        Vec3 target = leader.position().add(formationOffset(goose.getFormationIndex(), heading));
        Vec3 desired = leaderVelocity.add(target.subtract(goose.position()).scale(0.12));
        if (desired.length() > FOLLOW_MAX_SPEED) desired = desired.normalize().scale(FOLLOW_MAX_SPEED);
        Vec3 velocity = goose.getDeltaMovement();
        goose.setDeltaMovement(velocity.add(desired.subtract(velocity).scale(ACCEL)));
        float yaw = yawOf(heading);
        goose.setYRot(yaw);
        goose.yBodyRot = yaw;
        goose.yHeadRot = yaw;
    }

    static Vec3 formationOffset(int index, Vec3 heading) {
        int rank = (index + 1) / 2;
        double side = index % 2 == 1 ? 1 : -1;
        Vec3 lateral = new Vec3(-heading.z, 0, heading.x).scale(2.4 * rank * side);
        return heading.scale(-2.8 * rank).add(lateral).add(0, 0.3 * rank, 0);
    }

    private static Vec3 headingVec(Goose goose) {
        float rad = goose.getYRot() * ((float) Math.PI / 180F);
        return new Vec3(-Mth.sin(rad), 0, Mth.cos(rad));
    }

    private static double terrainAhead(ServerLevel level, Goose goose, Vec3 direction) {
        int here = level.getHeight(Heightmap.Types.MOTION_BLOCKING, goose.getBlockX(), goose.getBlockZ());
        int near = level.getHeight(Heightmap.Types.MOTION_BLOCKING,
                Mth.floor(goose.getX() + direction.x * 16), Mth.floor(goose.getZ() + direction.z * 16));
        int far = level.getHeight(Heightmap.Types.MOTION_BLOCKING,
                Mth.floor(goose.getX() + direction.x * 32), Mth.floor(goose.getZ() + direction.z * 32));
        return Math.max(here, Math.max(near, far));
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
        return headingVec(goose);
    }

    private static float yawOf(Vec3 heading) {
        return (float) (Mth.atan2(heading.z, heading.x) * (180.0 / Math.PI)) - 90.0F;
    }
}
