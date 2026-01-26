package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;

public class MooseMeleeAttackGoal extends MeleeAttackGoal {

    private final Moose moose;

    private LivingEntity cachedTarget;
    private boolean isReadyingAttack;
    private int attackDelay;

    public MooseMeleeAttackGoal(Moose moose, double speedModifier) {
        super(moose, speedModifier, false);
        this.moose = moose;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        var target = moose.getTarget();
        if (target == null) {
            return false;
        }
        if (!moose.targetMemory.isUpsetAt(target)) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (isReadyingAttack) {
            return true;
        }
        var target = moose.getTarget();
        if (target == null) {
            return false;
        }
        if (!moose.targetMemory.isUpsetAt(target)) {
            return false;
        }
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        if (isReadyingAttack) {
            if (attackDelay > 0) {
                attackDelay--;
                if (attackDelay == 0) {
                    isReadyingAttack = false;
                    if (cachedTarget == null || cachedTarget.isDeadOrDying()) {
                        cachedTarget = null;
                    }
                    if (cachedTarget != null) {
                        if (canDamageCachedTarget(cachedTarget)) {
                            moose.doHurtTarget(cachedTarget);
                            resetAttackCooldown();
                            cachedTarget = null;
                        }
                    }
                }
            }
        }
        super.tick();
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (isReadyingAttack) {
            return;
        }
        if (canPerformAttack(target)) {
            moose.level().broadcastEntityEvent(moose, Moose.ATTACK_EVENT);
            moose.setStompCooldown();
            cachedTarget = target;
            isReadyingAttack = true;
            attackDelay = 16;
        }
    }

    @Override
    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = this.adjustedTickDelay(80);
    }

    protected boolean canDamageCachedTarget(LivingEntity cachedTarget) {
        if (!isTimeToAttack()) {
            return false;
        }
        return cachedTarget.distanceTo(moose) < 6f && moose.getSensing().hasLineOfSight(cachedTarget);
    }
}