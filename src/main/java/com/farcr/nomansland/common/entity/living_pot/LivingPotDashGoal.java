package com.farcr.nomansland.common.entity.living_pot;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LivingPotDashGoal extends Goal {

    private static final double DASH_RANGE = 10.0;
    private static final double DASH_SPEED = 4.0;
    private static final int DASH_COOLDOWN = 60;
    private static final int WINDUP_TICKS = 10;
    private static final int MAX_CHARGE_TICKS = 20;
    private static final double DASH_DISTANCE = 3.0;
    private static final double HIT_RANGE_SQ = 1.8 * 1.8;

    private final LivingPot pot;
    private Vec3 dashDir = Vec3.ZERO;
    private Vec3 chargeStart = Vec3.ZERO;
    private Vec3 dashDest = Vec3.ZERO;
    private int dashTick = 0;
    private int cooldown = 0;
    private boolean charging = false;
    private boolean hasHitTarget = false;

    public LivingPotDashGoal(LivingPot pot) {
        this.pot = pot;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!pot.isLarge()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        LivingEntity target = pot.getTarget();
        if (target == null || !target.isAlive()) return false;
        return pot.distanceToSqr(target) <= DASH_RANGE * DASH_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        return pot.isDashing;
    }

    @Override
    public void start() {
        LivingEntity target = pot.getTarget();
        if (target == null) return;

        pot.isDashing = true;
        charging = false;
        hasHitTarget = false;
        dashTick = 0;

        dashDir = target.position().subtract(pot.position()).normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(-dashDir.x, dashDir.z));
        pot.setYRot(yaw);
        pot.yRotO = yaw;
        pot.setYBodyRot(yaw);

        pot.getNavigation().stop();
        pot.dashStartAnimState.start(pot.tickCount);

        pot.level().playSound(null, pot.getX(), pot.getY(), pot.getZ(),
                SoundEvents.DECORATED_POT_HIT, SoundSource.HOSTILE,
                1.2F, 0.5F);
    }

    @Override
    public void tick() {
        dashTick++;

        if (!charging) {
            float yaw = (float) Math.toDegrees(Math.atan2(-dashDir.x, dashDir.z));
            pot.setYRot(yaw);
            pot.setYBodyRot(yaw);

            if (dashTick >= WINDUP_TICKS) {
                charging = true;
                chargeStart = pot.position();
                dashDest = chargeStart.add(dashDir.scale(DASH_DISTANCE));
                dashTick = 0;

                pot.dashStartAnimState.stop();
                pot.dashLoopAnimState.start(pot.tickCount);

                pot.level().playSound(null, pot.getX(), pot.getY(), pot.getZ(),
                        SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE,
                        0.6F, 1.4F + pot.getRandom().nextFloat() * 0.2F);
            }
            return;
        }

        pot.getMoveControl().setWantedPosition(dashDest.x, dashDest.y, dashDest.z, DASH_SPEED);

        if (hitWall()) {
            pot.hurt(pot.damageSources().fall(), 3.0F);

            if (pot.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TERRACOTTA.defaultBlockState()),
                        pot.getX(), pot.getY() + 0.5, pot.getZ(),
                        10, 0.3, 0.3, 0.3, 0.05);
            }
            pot.level().playSound(null, pot.getX(), pot.getY(), pot.getZ(),
                    SoundEvents.DECORATED_POT_SHATTER, SoundSource.HOSTILE,
                    1.0F, 0.7F);
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
        dashTick = 0;
        charging = false;
        hasHitTarget = false;
        pot.dashStartAnimState.stop();
        pot.dashLoopAnimState.stop();
        pot.dashEndAnimState.start(pot.tickCount);
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
