package com.farcr.nomansland.common.entity.living_pot;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LivingPotDashGoal extends Goal {

    private static final double DASH_RANGE = 10.0;
    private static final double DASH_SPEED = 2.5;
    private static final int DASH_COOLDOWN = 60;
    private static final int MAX_DASH_TICKS = 30;
    private static final double DASH_DISTANCE = 3.0;
    private static final double HIT_RANGE_SQ = 1.8 * 1.8;

    private final LivingPot pot;
    private Vec3 dashDir = Vec3.ZERO;
    private Vec3 dashStart = Vec3.ZERO;
    private Vec3 dashDest = Vec3.ZERO;
    private int dashTick = 0;
    private int cooldown = 0;

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

        dashStart = pot.position();
        dashDir = target.position().subtract(pot.position()).normalize();
        dashDest = dashStart.add(dashDir.scale(DASH_DISTANCE));
        pot.isDashing = true;
        dashTick = 0;

        float yaw = (float) Math.toDegrees(Math.atan2(-dashDir.x, dashDir.z));
        pot.setYRot(yaw);
        pot.yRotO = yaw;
        pot.setYBodyRot(yaw);

        pot.getNavigation().stop();
        pot.dashStartAnimState.start(pot.tickCount);
    }

    @Override
    public void tick() {
        dashTick++;

        if (dashTick == 5) {
            pot.dashStartAnimState.stop();
            pot.dashLoopAnimState.start(pot.tickCount);
        }

        pot.getMoveControl().setWantedPosition(dashDest.x, dashDest.y, dashDest.z, DASH_SPEED);

        if (hitWall()) {
            pot.hurt(pot.damageSources().fall(), 10.0F);
            stop();
            return;
        }

        LivingEntity target = pot.getTarget();
        if (target != null && pot.distanceToSqr(target) <= HIT_RANGE_SQ) {
            boolean hit = target.hurt(pot.damageSources().mobAttack(pot),
                    (float) pot.getAttributeValue(Attributes.ATTACK_DAMAGE));
//            if (hit) pot.setLastAttackTick(pot.tickCount);
        }

        double traveled = pot.position().distanceTo(dashStart);
        if (traveled >= DASH_DISTANCE || dashTick >= MAX_DASH_TICKS) {
            stop();
        }
    }

    @Override
    public void stop() {
        pot.isDashing = false;
        cooldown = DASH_COOLDOWN;
        dashTick = 0;
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
