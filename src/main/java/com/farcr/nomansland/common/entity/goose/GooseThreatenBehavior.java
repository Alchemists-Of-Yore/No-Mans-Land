package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GooseThreatenBehavior extends Behavior<Goose> {
    private static final int BASE_PATIENCE = 70;
    private static final int CONFIDENCE_BONUS = 18;
    private static final int MIN_PATIENCE = 20;
    private static final float BACKPEDAL_SPEED = 0.45F;
    private static final double LOST_THREAT_SQR = 64.0;
    private static final double FLEE_TRIGGER_SQR = 16.0;
    private static final float FLEE_SPEED = 1.4F;
    private static final double FLEE_DISTANCE = 8.0;
    private static final double ADULT_SEARCH_RADIUS = 16.0;
    private static final double HIDE_BEHIND_PARENT = 1.5;

    private int threatenTicks;

    public GooseThreatenBehavior() {
        super(Map.of(
                MemoryModuleType.AVOID_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT
        ), Integer.MAX_VALUE);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return goose.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET)
                && goose.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty();
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        threatenTicks = 0;
        goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        LivingEntity threat = goose.getBrain().getMemory(MemoryModuleType.AVOID_TARGET).orElse(null);
        if (threat == null) return;

        if (!threat.isAlive() || goose.distanceToSqr(threat) > LOST_THREAT_SQR) {
            goose.getBrain().eraseMemory(MemoryModuleType.AVOID_TARGET);
            return;
        }
        goose.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, threat, 40L);
        goose.getLookControl().setLookAt(threat, 30.0F, 30.0F);

        if (goose.canFight()) {
            menace(goose, threat);
            threatenTicks++;
            boolean provoked = goose.getLastHurtByMob() == threat;
            int patience = Math.max(MIN_PATIENCE, BASE_PATIENCE - CONFIDENCE_BONUS * goose.flockConfidence());
            if (provoked || (threatenTicks >= patience && goose.isAttackReady())) {
                goose.beginAttack(threat);
                goose.rallyFlock(threat);
            }
        } else {
            Goose parent = goose.isBaby() ? nearestAdult(goose) : null;
            if (parent != null) {
                hideBehind(goose, parent, threat);
            } else if (goose.distanceToSqr(threat) < FLEE_TRIGGER_SQR) {
                flee(goose, threat);
            }
        }
    }

    private static void menace(Goose goose, LivingEntity threat) {
        double dx = threat.getX() - goose.getX();
        double dz = threat.getZ() - goose.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        goose.setYRot(yaw);
        goose.yBodyRot = yaw;
        goose.getMoveControl().strafe(-BACKPEDAL_SPEED, 0.0F);
    }

    private static void flee(Goose goose, LivingEntity threat) {
        Vec3 away = goose.position().subtract(threat.position()).normalize().scale(FLEE_DISTANCE).add(goose.position());
        BehaviorUtils.setWalkAndLookTargetMemories(goose, BlockPos.containing(away), FLEE_SPEED, 0);
    }

    private static void hideBehind(Goose baby, Goose parent, LivingEntity threat) {
        Vec3 awayFromThreat = parent.position().subtract(threat.position()).normalize().scale(HIDE_BEHIND_PARENT);
        Vec3 hideSpot = parent.position().add(awayFromThreat);
        BehaviorUtils.setWalkAndLookTargetMemories(baby, BlockPos.containing(hideSpot), FLEE_SPEED, 0);
        baby.getLookControl().setLookAt(threat, 30.0F, 30.0F);
    }

    @Nullable
    private static Goose nearestAdult(Goose baby) {
        Goose closest = null;
        double best = Double.MAX_VALUE;
        for (Goose other : baby.nearbyGeese(ADULT_SEARCH_RADIUS)) {
            if (other.isBaby()) continue;
            double distance = baby.distanceToSqr(other);
            if (distance < best) {
                best = distance;
                closest = other;
            }
        }
        return closest;
    }
}
