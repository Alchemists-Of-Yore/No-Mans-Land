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

    private static final int LEISURE_CHANCE = 1200;
    private static final int LEISURE_COOLDOWN = 2400;
    private static final double PANIC_RANGE_SQR = 144.0;
    private static final double PRESSED_RANGE_SQR = 36.0;
    private static final int WATER_SEARCH_RADIUS = 20;
    private static final double MIN_WATER_TRIP_SQR = 64.0;
    private static final double ESCAPE_DISTANCE = 24.0;
    private static final float TAKEOFF_SPEED = 0.3F;
    private static final float TAKEOFF_LIFT = 0.18F;
    private static final float FLY_SPEED = 0.4F;
    private static final float LAND_SPEED = 0.26F;
    private static final float FLY_TURN_RATE = 8.0F;
    private static final float LAND_TURN_RATE = 6.0F;
    private static final int TERRAIN_CLEARANCE = 5;
    private static final double TAKEOFF_HEIGHT = 3.0;
    private static final int MAX_TAKEOFF_TICKS = 20;
    private static final int MAX_FLY_TICKS = 300;
    private static final int MAX_LAND_TICKS = 240;
    private static final double LAND_DISTANCE_SQR = 16.0;
    private static final double SPIRAL_RADIUS_SQR = 4.0;
    private static final int MAX_GROUND_TICKS = 3;
    private static final double CONTINUE_DISTANCE_SQR = 9.0;
    private static final float CONTINUE_SPEED = 1.1F;

    private Phase phase = Phase.TAKEOFF;
    @Nullable
    private Vec3 destination;
    private float flightYaw;
    private double takeoffY;
    private int phaseTicks;
    private int groundTicks;
    private boolean escaping;
    private long nextLeisureTime;

    public GooseFlightBehavior() {
        super(Map.of(), Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isBaby() || goose.isFlying() || goose.isPassenger() || goose.isLeashed() || goose.isInLove()) return false;
        Brain<Goose> brain = goose.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) || brain.hasMemoryValue(MemoryModuleType.BREED_TARGET)) return false;

        LivingEntity danger = pressingThreat(goose);
        if (danger != null) {
            destination = escapeDestination(goose, danger);
            escaping = true;
            return true;
        }

        if (!goose.isCarrying() && !goose.isStealing() && !goose.isDrinking() && goose.onGround()
                && level.getGameTime() >= nextLeisureTime
                && goose.getRandom().nextInt(LEISURE_CHANCE) == 0) {
            destination = leisureDestination(goose);
            escaping = false;
            return destination != null;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        Brain<Goose> brain = goose.getBrain();
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        brain.eraseMemory(MemoryModuleType.PATH);
        goose.getNavigation().stop();
        goose.setFlying(true);
        if (escaping) goose.honkAfraid();
        else goose.honk();
        phase = Phase.TAKEOFF;
        phaseTicks = 0;
        flightYaw = goose.getYRot();
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

        switch (phase) {
            case TAKEOFF -> takeoff(goose);
            case FLY -> fly(level, goose);
            case LAND -> land(level, goose);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        goose.setFlying(false);
        nextLeisureTime = gameTime + LEISURE_COOLDOWN + goose.getRandom().nextInt(LEISURE_COOLDOWN);
        if (destination != null
                && !goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && horizontalDistanceSqr(goose, destination) > CONTINUE_DISTANCE_SQR) {
            goose.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                    new WalkTarget(BlockPos.containing(destination), CONTINUE_SPEED, 1));
        } else {
            goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
        destination = null;
    }

    private void takeoff(Goose goose) {
        turnToward(goose, destination, FLY_TURN_RATE);
        Vec3 direction = yawDirection();
        goose.setDeltaMovement(direction.scale(TAKEOFF_SPEED).add(0, TAKEOFF_LIFT, 0));
        applyRotation(goose);

        if (goose.getY() >= takeoffY + TAKEOFF_HEIGHT || phaseTicks > MAX_TAKEOFF_TICKS) {
            enterPhase(Phase.FLY);
        }
    }

    private void fly(ServerLevel level, Goose goose) {
        if (destination == null) {
            enterPhase(Phase.LAND);
            return;
        }
        if (goose.horizontalCollision || goose.onGround()) {
            if (++groundTicks > MAX_GROUND_TICKS) {
                goose.setFlying(false);
                return;
            }
        } else {
            groundTicks = 0;
        }
        turnToward(goose, destination, FLY_TURN_RATE);
        Vec3 direction = yawDirection();
        double desiredY = clearanceAhead(level, goose, direction) + TERRAIN_CLEARANCE;
        double lift = Mth.clamp((desiredY - goose.getY()) * 0.12, -0.5 * FLY_SPEED, 0.55 * FLY_SPEED);
        goose.setDeltaMovement(direction.scale(FLY_SPEED).add(0, lift, 0));
        applyRotation(goose);

        if (horizontalDistanceSqr(goose, destination) < LAND_DISTANCE_SQR || phaseTicks > MAX_FLY_TICKS) {
            enterPhase(Phase.LAND);
        }
    }

    private void land(ServerLevel level, Goose goose) {
        Vec3 target = destination != null ? destination : goose.position();
        if (horizontalDistanceSqr(goose, target) < SPIRAL_RADIUS_SQR) {
            flightYaw += LAND_TURN_RATE;
        } else {
            turnToward(goose, target, LAND_TURN_RATE);
        }
        Vec3 direction = yawDirection();
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, goose.getBlockX(), goose.getBlockZ());
        double sink = Mth.clamp((groundY - goose.getY()) * 0.1, -0.12, 0.02);
        if (goose.getY() > groundY + 1) sink = Math.min(sink, -0.05);
        goose.setDeltaMovement(direction.scale(LAND_SPEED).add(0, sink, 0));
        applyRotation(goose);

        if (goose.onGround() || goose.isInWater() || goose.horizontalCollision || phaseTicks > MAX_LAND_TICKS) {
            goose.setFlying(false);
        }
    }

    private void enterPhase(Phase next) {
        phase = next;
        phaseTicks = 0;
        groundTicks = 0;
    }

    private void turnToward(Goose goose, @Nullable Vec3 target, float turnRate) {
        if (target == null) return;
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

    private static double clearanceAhead(ServerLevel level, Goose goose, Vec3 direction) {
        int here = level.getHeight(Heightmap.Types.MOTION_BLOCKING, goose.getBlockX(), goose.getBlockZ());
        int near = level.getHeight(Heightmap.Types.MOTION_BLOCKING,
                Mth.floor(goose.getX() + direction.x * 8), Mth.floor(goose.getZ() + direction.z * 8));
        int far = level.getHeight(Heightmap.Types.MOTION_BLOCKING,
                Mth.floor(goose.getX() + direction.x * 16), Mth.floor(goose.getZ() + direction.z * 16));
        return Math.max(here, Math.max(near, far));
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
        if (hurtBy != null && hurtBy.isAlive() && goose.distanceToSqr(hurtBy) < PANIC_RANGE_SQR) return hurtBy;
        LivingEntity avoided = brain.getMemory(MemoryModuleType.AVOID_TARGET).orElse(null);
        if (avoided != null && avoided.isAlive() && goose.distanceToSqr(avoided) < PRESSED_RANGE_SQR) return avoided;
        return null;
    }

    private static Vec3 escapeDestination(Goose goose, LivingEntity danger) {
        Vec3 away = goose.position().subtract(danger.position()).multiply(1, 0, 1);
        if (away.lengthSqr() < 1.0E-4) away = randomHorizontal(goose);
        float spread = (goose.getRandom().nextFloat() - 0.5F) * ((float) Math.PI / 3F);
        away = away.normalize().yRot(spread);
        return goose.position().add(away.scale(ESCAPE_DISTANCE + goose.getRandom().nextInt(12)));
    }

    @Nullable
    private static Vec3 leisureDestination(Goose goose) {
        BlockPos water = distantWater(goose);
        if (water != null) return Vec3.atBottomCenterOf(water);
        Vec3 direction = randomHorizontal(goose);
        return goose.position().add(direction.scale(16 + goose.getRandom().nextInt(12)));
    }

    @Nullable
    private static BlockPos distantWater(Goose goose) {
        BlockPos origin = goose.blockPosition();
        BlockPos closest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-WATER_SEARCH_RADIUS, -4, -WATER_SEARCH_RADIUS),
                origin.offset(WATER_SEARCH_RADIUS, 4, WATER_SEARCH_RADIUS))) {
            if (!goose.level().getFluidState(pos).is(FluidTags.WATER)) continue;
            double distance = origin.distSqr(pos);
            if (distance > MIN_WATER_TRIP_SQR && distance < best) {
                best = distance;
                closest = pos.immutable();
            }
        }
        return closest;
    }

    private static Vec3 randomHorizontal(Goose goose) {
        double angle = goose.getRandom().nextDouble() * Math.PI * 2;
        return new Vec3(Math.cos(angle), 0, Math.sin(angle));
    }
}
