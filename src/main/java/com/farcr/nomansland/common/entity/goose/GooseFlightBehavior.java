package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GooseFlightBehavior extends Behavior<Goose> {
    private enum Phase { TAKEOFF, FLY, LAND }

    private static final float FLY_SPEED = 0.7F;
    private static final float TURN_RATE = 9.0F;
    private static final float ACCEL = 0.14F;
    private static final double CLIMB_CAP = 0.4;
    private static final double LAND_CLIMB_CAP = 0.26;
    private static final double DESCENT_CAP = 0.5;
    private static final double FLY_HEIGHT = 6.0;
    private static final int FLIGHT_COOLDOWN = 120;

    private Phase phase = Phase.TAKEOFF;
    @Nullable
    private Vec3 destination;
    private double takeoffY;
    private int phaseTicks;
    private int stuckTicks;
    private boolean liftedOff;
    private long nextFlightTime;

    public GooseFlightBehavior() {
        super(Map.of(), Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isBaby() || goose.isFlying() || goose.isPassenger() || goose.isLeashed() || goose.isInLove()) return false;
        if (level.getGameTime() < nextFlightTime) return false;
        Brain<Goose> brain = goose.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) || brain.hasMemoryValue(MemoryModuleType.BREED_TARGET)) return false;
        if (!goose.onGround()) return false;

        LivingEntity danger = pressingThreat(goose);
        if (danger == null) return false;
        destination = escapeDestination(level, goose, danger);
        return true;
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        Brain<Goose> brain = goose.getBrain();
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        brain.eraseMemory(MemoryModuleType.PATH);
        goose.getNavigation().stop();
        goose.setFlying(true);
        goose.setDeltaMovement(goose.getDeltaMovement().add(0, 0.28, 0));
        goose.honkAfraid();
        phase = Phase.TAKEOFF;
        phaseTicks = 0;
        stuckTicks = 0;
        liftedOff = false;
        takeoffY = goose.getY();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return goose.isFlying() && !goose.isPassenger() && !goose.isLeashed()
                && !goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
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
            case FLY -> fly(level, goose);
            case LAND -> land(level, goose);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        goose.setFlying(false);
        nextFlightTime = gameTime + FLIGHT_COOLDOWN + goose.getRandom().nextInt(FLIGHT_COOLDOWN);
        if (destination != null
                && !goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && horizontalDistanceSqr(goose, destination) > 9.0) {
            goose.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                    new WalkTarget(BlockPos.containing(destination), 1.2F, 1));
        } else {
            goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
        destination = null;
    }

    private void takeoff(Goose goose) {
        GooseFlight.towardPoint(goose, destination.x, destination.z, takeoffY + FLY_HEIGHT, 0.4F, TURN_RATE, ACCEL, CLIMB_CAP);
        if (!liftedOff && phaseTicks > 8) {
            goose.setFlying(false);
            return;
        }
        if (goose.getY() >= takeoffY + 4.0 || phaseTicks > 30) {
            enterPhase(Phase.FLY);
        }
    }

    private void fly(ServerLevel level, Goose goose) {
        double dx = destination.x - goose.getX();
        double dz = destination.z - goose.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        double dirX = dist > 1.0E-4 ? dx / dist : 0;
        double dirZ = dist > 1.0E-4 ? dz / dist : 0;
        double targetY = GooseFlight.terrainAhead(level, goose.getX(), goose.getZ(), dirX, dirZ, 8, 16) + FLY_HEIGHT;
        GooseFlight.towardPoint(goose, destination.x, destination.z, targetY, FLY_SPEED, TURN_RATE, ACCEL, CLIMB_CAP);
        if (goose.horizontalCollision && ++stuckTicks > 8) {
            enterPhase(Phase.LAND);
            return;
        }
        if (!goose.horizontalCollision) stuckTicks = 0;
        if (horizontalDistanceSqr(goose, destination) < 49.0 || phaseTicks > 120) {
            enterPhase(Phase.LAND);
        }
    }

    private void land(ServerLevel level, Goose goose) {
        Vec3 target = destination != null ? destination : goose.position();
        double groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, goose.getBlockX(), goose.getBlockZ());
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
            float speed = (float) Mth.clamp(dist * 0.12, 0.18, FLY_SPEED);
            GooseFlight.approach(goose, target.x, target.z, targetY, speed, TURN_RATE, ACCEL, LAND_CLIMB_CAP, DESCENT_CAP);
        }
        if (phaseTicks > 80) {
            goose.setFlying(false);
        }
    }

    private void enterPhase(Phase next) {
        phase = next;
        phaseTicks = 0;
        stuckTicks = 0;
    }

    private static double horizontalDistanceSqr(Goose goose, Vec3 target) {
        double dx = target.x - goose.getX();
        double dz = target.z - goose.getZ();
        return dx * dx + dz * dz;
    }

    @Nullable
    private static LivingEntity pressingThreat(Goose goose) {
        if (goose.canFight()) return null;
        Brain<Goose> brain = goose.getBrain();
        LivingEntity hurtBy = brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).orElse(null);
        if (hurtBy != null && hurtBy.isAlive() && goose.distanceToSqr(hurtBy) < 144.0) return hurtBy;
        LivingEntity avoided = brain.getMemory(MemoryModuleType.AVOID_TARGET).orElse(null);
        if (avoided != null && avoided.isAlive() && goose.distanceToSqr(avoided) < 36.0) return avoided;
        return flockPanicThreat(goose);
    }

    @Nullable
    private static LivingEntity flockPanicThreat(Goose goose) {
        if (goose.getRandom().nextInt(2) == 0) return null;
        for (Goose flockmate : goose.nearbyGeese(8.0)) {
            LivingEntity attacker = flockmate.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).orElse(null);
            if (attacker != null && attacker.isAlive() && goose.distanceToSqr(attacker) < 100.0) return attacker;
        }
        return null;
    }

    private static Vec3 escapeDestination(ServerLevel level, Goose goose, LivingEntity danger) {
        Vec3 away = goose.position().subtract(danger.position()).multiply(1, 0, 1);
        if (away.lengthSqr() < 1.0E-4) {
            double angle = goose.getRandom().nextDouble() * Math.PI * 2;
            away = new Vec3(Math.cos(angle), 0, Math.sin(angle));
        }
        float spread = (goose.getRandom().nextFloat() - 0.5F) * ((float) Math.PI / 3F);
        away = away.normalize().yRot(spread);
        Vec3 water = waterToward(level, goose, away);
        if (water != null) return water;
        return goose.position().add(away.scale(22.0 + goose.getRandom().nextInt(10)));
    }

    @Nullable
    private static Vec3 waterToward(ServerLevel level, Goose goose, Vec3 direction) {
        Vec3 side = new Vec3(-direction.z, 0, direction.x);
        Vec3 best = null;
        double bestScore = Double.MAX_VALUE;
        for (int forward = 12; forward <= 32; forward += 4) {
            for (int lateral = -8; lateral <= 8; lateral += 4) {
                Vec3 point = goose.position().add(direction.scale(forward)).add(side.scale(lateral));
                int x = Mth.floor(point.x);
                int z = Mth.floor(point.z);
                if (!level.isLoaded(new BlockPos(x, level.getMinBuildHeight(), z))) continue;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (!level.getFluidState(new BlockPos(x, surfaceY - 1, z)).is(FluidTags.WATER)) continue;
                double score = Math.abs(forward - 24) + Math.abs(lateral);
                if (score < bestScore) {
                    bestScore = score;
                    best = new Vec3(x + 0.5, surfaceY, z + 0.5);
                }
            }
        }
        return best;
    }
}
