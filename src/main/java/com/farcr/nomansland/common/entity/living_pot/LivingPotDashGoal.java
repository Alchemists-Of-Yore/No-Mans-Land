package com.farcr.nomansland.common.entity.living_pot;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LivingPotDashGoal extends Goal {

    private static final double DASH_RANGE = 4.0;
    private static final double DASH_SPEED = 4.0;
    private static final int DASH_COOLDOWN = 60;
    private static final int WINDUP_TICKS = 4;
    private static final int MAX_CHARGE_TICKS = 20;
    private static final double DASH_DISTANCE = 4.0;
    private static final double HIT_RANGE_SQ = 1.8 * 1.8;
    private static final int POST_DASH_LOCK_TICKS = 5;

    private final LivingPot pot;
    private Vec3 dashDir = Vec3.ZERO;
    private Vec3 chargeStart = Vec3.ZERO;
    private Vec3 dashDest = Vec3.ZERO;
    private int dashTick = 0;
    private int cooldown = 0;
    private boolean charging = false;
    private boolean hasHitTarget = false;
    private float lockedYaw;
    private int postDashLockTicks = 0;

    public LivingPotDashGoal(LivingPot pot) {
        this.pot = pot;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!pot.isLarge()) return false;
        if (postDashLockTicks > 0) {
            pot.setYRot(lockedYaw);
            pot.setYBodyRot(lockedYaw);
            postDashLockTicks--;
            cooldown--;
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (pot.getLastKnownTargetPos() != null) {
            return true;
        }
        LivingEntity target = pot.getTarget();
        if (target == null || !target.isAlive() || !target.canBeSeenAsEnemy() || target.isInvisible()) return false;
        return pot.distanceToSqr(target) <= DASH_RANGE * DASH_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        return pot.isDashing;
    }

    @Override
    public void start() {
        Vec3 targetPos;
        if (pot.getLastKnownTargetPos() != null) {
            targetPos = pot.getLastKnownTargetPos();
            pot.setLastKnownTargetPos(null);
        } else {
            LivingEntity target = pot.getTarget();
            if (target == null) return;
            targetPos = target.position();
        }

        pot.isDashing = true;
        charging = false;
        hasHitTarget = false;
        dashTick = 0;

        dashDir = targetPos.subtract(pot.position()).normalize();

        lockedYaw = (float) Math.toDegrees(Math.atan2(-dashDir.x, dashDir.z));
        pot.setYRot(lockedYaw);
        pot.yRotO = lockedYaw;
        pot.setYBodyRot(lockedYaw);

        pot.getNavigation().stop();
        pot.startDashStart();

        pot.level().playSound(null, pot.getX(), pot.getY(), pot.getZ(),
                SoundEvents.DECORATED_POT_HIT, SoundSource.HOSTILE,
                1.2F, 0.5F);
    }

    @Override
    public void tick() {
        dashTick++;

        if (!charging) {
            pot.setYRot(lockedYaw);
            pot.setYBodyRot(lockedYaw);

            if (dashTick >= WINDUP_TICKS) {
                charging = true;
                chargeStart = pot.position();
                dashDest = chargeStart.add(dashDir.scale(DASH_DISTANCE));
                dashTick = 0;

                pot.startDashLoop();

                pot.level().playSound(null, pot.getX(), pot.getY(), pot.getZ(),
                        SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE,
                        0.6F, 1.4F + pot.getRandom().nextFloat() * 0.2F);
            }
            return;
        }

        pot.setYRot(lockedYaw);
        pot.setYBodyRot(lockedYaw);
        pot.getMoveControl().setWantedPosition(dashDest.x, dashDest.y, dashDest.z, DASH_SPEED);

        if (hitWall()) {
            pot.hurt(pot.damageSources().fall(), 3.0F);

            pot.spawnShatterParticles(20, 0.4);
            pot.level().playSound(null, pot.getX(), pot.getY(), pot.getZ(),
                    SoundEvents.DECORATED_POT_SHATTER, SoundSource.HOSTILE,
                    1.2F, 0.5F + pot.getRandom().nextFloat() * 0.2F);
            stop();
            return;
        }

        if (!hasHitTarget) {
            LivingEntity target = pot.getTarget();
            if (target != null && pot.distanceToSqr(target) <= HIT_RANGE_SQ) {
                float dashDamage = (float) (pot.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5);
                boolean hit = target.hurt(pot.damageSources().mobAttack(pot), dashDamage);
                if (hit) {
                    Vec3 knockback = dashDir.scale(0.5).add(0, 0.2, 0);
                    target.push(knockback.x, knockback.y, knockback.z);
                }
                hasHitTarget = true;
            }
        }

        double traveled = pot.position().distanceTo(chargeStart);
        if (traveled >= DASH_DISTANCE || dashTick >= MAX_CHARGE_TICKS) {
            stop();
        }
    }

    @Override
    public void stop() {
        pot.isDashing = false;
        pot.meleeCooldownTicks = 20;
        cooldown = DASH_COOLDOWN;
        postDashLockTicks = POST_DASH_LOCK_TICKS;
        dashTick = 0;
        charging = false;
        hasHitTarget = false;
        pot.startDashEnd();
    }

    private boolean hitWall() {
        BlockHitResult hitLow = pot.level().clip(new ClipContext(
                pot.position().add(0, 0.1, 0), pot.position().add(0, 0.1, 0).add(dashDir.scale(0.6)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pot));
        BlockHitResult hitHigh = pot.level().clip(new ClipContext(
                pot.position().add(0, 1.5, 0), pot.position().add(0, 1.5, 0).add(dashDir.scale(0.6)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pot));

        return hitLow.getType() == HitResult.Type.BLOCK && hitHigh.getType() == HitResult.Type.BLOCK;
    }
}
