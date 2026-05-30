package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class GooseAttackBehavior extends Behavior<Goose> {
    private static final int ATTACK_COOLDOWN = 20;
    private static final int DISENGAGE_COOLDOWN = 80;
    private static final int MAX_RETURN_TICKS = 100;
    private static final int MAX_CHASE_TICKS = 100;
    private static final float CHASE_SPEED = 1.45F;
    private static final float RETURN_SPEED = 1.2F;
    private static final double ANCHOR_REACHED_SQR = 6.25;
    private static final float RETREAT_HEALTH_FRACTION = 0.6F;

    private boolean landedHit;
    private boolean returning;
    private int attackCooldown;
    private int returnTicks;
    private int chaseTicks;
    private int hurtStampAtStart;

    public GooseAttackBehavior() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), Integer.MAX_VALUE);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        LivingEntity target = goose.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive();
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        landedHit = false;
        returning = false;
        attackCooldown = 0;
        returnTicks = 0;
        chaseTicks = 0;
        hurtStampAtStart = goose.getLastHurtByMobTimestamp();
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        LivingEntity target = goose.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) return;
        if (attackCooldown > 0) attackCooldown--;

        if (!returning && (landedHit || wasThrashed(goose))) {
            returning = true;
        }

        if (returning) {
            returnToAnchor(goose);
        } else {
            chaseAndPeck(goose, target);
        }
    }

    private boolean wasThrashed(Goose goose) {
        return goose.getLastHurtByMobTimestamp() != hurtStampAtStart
                && goose.getHealth() < goose.getMaxHealth() * RETREAT_HEALTH_FRACTION;
    }

    private void chaseAndPeck(Goose goose, LivingEntity target) {
        if (++chaseTicks > MAX_CHASE_TICKS) {
            returning = true;
            return;
        }
        BehaviorUtils.setWalkAndLookTargetMemories(goose, target, CHASE_SPEED, 0);
        if (attackCooldown == 0 && goose.isWithinMeleeAttackRange(target)) {
            goose.swing(InteractionHand.MAIN_HAND);
            goose.doHurtTarget(target);
            attackCooldown = ATTACK_COOLDOWN;
            landedHit = true;
        }
    }

    private void returnToAnchor(Goose goose) {
        BlockPos anchor = goose.getAggressionAnchor();
        if (anchor == null || ++returnTicks > MAX_RETURN_TICKS || goose.blockPosition().distSqr(anchor) <= ANCHOR_REACHED_SQR) {
            goose.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            return;
        }
        BehaviorUtils.setWalkAndLookTargetMemories(goose, anchor, RETURN_SPEED, 2);
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        Brain<Goose> brain = goose.getBrain();
        brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        goose.setTarget(null);
        goose.setAttackCooldown(DISENGAGE_COOLDOWN);
        if (landedHit) goose.flapBriefly();
    }
}
