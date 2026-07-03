package com.farcr.nomansland.common.entity.goose;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GooseChaseFlightBehavior extends Behavior<Goose> {
    private enum Phase { TAKEOFF, PURSUE }

    private static final float CHASE_SPEED = 0.72F;
    private static final float TURN_RATE = 11.0F;
    private static final float ACCEL = 0.16F;
    private static final double CLIMB_CAP = 0.45;
    private static final double DESCENT_CAP = 0.5;
    private static final double HOVER_ABOVE = 1.2;
    private static final double TAKEOFF_GAIN = 3.0;
    private static final double GIVE_UP_RANGE_SQR = 32.0 * 32.0;
    private static final double ELEVATED_TRIGGER = 1.6;
    private static final long UNREACHABLE_TICKS = 30L;
    private static final int ATTACK_COOLDOWN = 18;
    private static final int MAX_CHASE_TICKS = 160;
    private static final int CHASE_COOLDOWN = 60;

    private Phase phase = Phase.TAKEOFF;
    private double takeoffY;
    private int phaseTicks;
    private int totalTicks;
    private int attackCooldown;
    private boolean liftedOff;
    private long nextChaseTime;

    public GooseChaseFlightBehavior() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isBaby() || goose.isFlying() || goose.isPassenger() || goose.isLeashed()) return false;
        if (level.getGameTime() < nextChaseTime) return false;
        if (!goose.onGround() || !goose.canFight()) return false;
        Brain<Goose> brain = goose.getBrain();
        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null || !target.isAlive()) return false;
        if (goose.isWithinMeleeAttackRange(target)) return false;
        if (goose.distanceToSqr(target) > GIVE_UP_RANGE_SQR) return false;

        boolean unreachable = brain.getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                .map(since -> level.getGameTime() - since > UNREACHABLE_TICKS).orElse(false);
        boolean elevated = target.getY() - goose.getY() > ELEVATED_TRIGGER;
        return unreachable || elevated;
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        Brain<Goose> brain = goose.getBrain();
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.PATH);
        brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        goose.getNavigation().stop();
        goose.setFlying(true);
        goose.setDeltaMovement(goose.getDeltaMovement().add(0, 0.28, 0));
        goose.honkAngry();
        phase = Phase.TAKEOFF;
        phaseTicks = 0;
        totalTicks = 0;
        attackCooldown = 0;
        liftedOff = false;
        takeoffY = goose.getY();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        if (totalTicks > MAX_CHASE_TICKS) return false;
        LivingEntity target = goose.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive() && goose.isFlying()
                && !goose.isPassenger() && !goose.isLeashed();
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        Brain<Goose> brain = goose.getBrain();
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        goose.markFlightControl();
        goose.resetFallDistance();
        phaseTicks++;
        totalTicks++;
        if (attackCooldown > 0) attackCooldown--;

        if (!goose.onGround()) liftedOff = true;
        if (liftedOff && (goose.onGround() || goose.isInWater())) {
            goose.setFlying(false);
            return;
        }

        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) {
            goose.setFlying(false);
            return;
        }

        switch (phase) {
            case TAKEOFF -> takeoff(goose, target);
            case PURSUE -> pursue(goose, target);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        goose.setFlying(false);
        goose.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        nextChaseTime = gameTime + CHASE_COOLDOWN;
    }

    private void takeoff(Goose goose, LivingEntity target) {
        double targetY = Math.max(takeoffY, target.getY()) + TAKEOFF_GAIN;
        GooseFlight.towardPoint(goose, target.getX(), target.getZ(), targetY, 0.42F, TURN_RATE, ACCEL, CLIMB_CAP);
        if (!liftedOff && phaseTicks > 10) {
            goose.setFlying(false);
            return;
        }
        if (goose.getY() >= takeoffY + TAKEOFF_GAIN || phaseTicks > 28) {
            phase = Phase.PURSUE;
            phaseTicks = 0;
        }
    }

    private void pursue(Goose goose, LivingEntity target) {
        double dx = target.getX() - goose.getX();
        double dz = target.getZ() - goose.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz * horiz > GIVE_UP_RANGE_SQR) {
            goose.setFlying(false);
            return;
        }
        double targetY = target.getY() + (horiz > 2.5 ? HOVER_ABOVE : 0.15);
        Vec3 aim = horiz > 3.0 ? target.position().add(target.getDeltaMovement().scale(6.0)) : target.position();
        GooseFlight.towardPoint(goose, aim.x, aim.z, targetY, CHASE_SPEED, TURN_RATE, ACCEL, CLIMB_CAP);

        if (attackCooldown == 0 && goose.isWithinMeleeAttackRange(target)) {
            goose.peck();
            goose.doHurtTarget(target);
            goose.honkAngry();
            attackCooldown = ATTACK_COOLDOWN;
            Vec3 lift = goose.getDeltaMovement();
            goose.setDeltaMovement(lift.x, Math.min(lift.y + 0.18, DESCENT_CAP), lift.z);
        }
    }
}
