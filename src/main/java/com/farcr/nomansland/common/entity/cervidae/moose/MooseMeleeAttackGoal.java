package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;

public class MooseMeleeAttackGoal extends MeleeAttackGoal {

    private final Moose moose;
    protected final PathNavigation pathNav;

    private LivingEntity cachedTarget;
    private boolean isReadyingAttack;
    private int attackDelay;

    public MooseMeleeAttackGoal(Moose moose, double speedModifier) {
        super(moose, speedModifier, false);
        this.moose = moose;
        this.pathNav = moose.getNavigation();
    }

    @Override
    public boolean canUse() {
        var target = moose.getTarget();
        if (target == null) {
            return false;
        }
        if (!target.isAlive()) {
            return false;
        }
        if (!moose.targetMemory.isUpsetAt(target)) {
            return false;
        }
        long time = moose.level().getGameTime();

        if (isReadyingAttack || time - lastCanUseCheck > 4L) {
            lastCanUseCheck = time;
            path = pathNav.createPath(target, 0);
            return path != null;
        } else {
            return false;
        }
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
        if (!target.isAlive()) {
            return false;
        }
        if (!moose.targetMemory.isUpsetAt(target)) {
            return false;
        }
        if (moose.distanceTo(target) > Moose.ACTIVE_AGGRO_DISTANCE) {
            return false;
        }
        if (followingTargetEvenIfNotSeen) {
            if (moose.isWithinRestriction(target.blockPosition())) {
                if (target.isSpectator()) {
                    return false;
                }
                if (target instanceof Player player) {
                    return !player.isCreative();
                }
                return true;
            }
            return false;
        } else {
            return !pathNav.isDone();
        }
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
        moose.lookAtAndFaceTarget(moose.getTarget());
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
        this.ticksUntilNextAttack = this.adjustedTickDelay(60);
    }

    protected boolean canDamageCachedTarget(LivingEntity cachedTarget) {
        if (!isTimeToAttack()) {
            return false;
        }
        return cachedTarget.distanceTo(moose) < 6f && moose.getSensing().hasLineOfSight(cachedTarget);
    }
}