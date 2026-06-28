package com.farcr.nomansland.common.entity.centipede;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CentipedeAttackGoal extends Goal {
    private final Centipede centipede;
    private int attackCooldown;
    private int lungeCooldown;
    private int rearCooldown;
    private int repathTimer;
    private int repositionTicks;
    private int orbitDir;

    public CentipedeAttackGoal(Centipede centipede) {
        this.centipede = centipede;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.centipede.getTarget();
        return target != null && target.isAlive() && !this.centipede.isBurrowed() && !this.centipede.isShriveling();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.repathTimer = 0;
        this.repositionTicks = 0;
        this.orbitDir = this.centipede.getRandom().nextBoolean() ? 1 : -1;
    }

    @Override
    public void stop() {
        this.centipede.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.centipede.getTarget();
        if (target == null) return;

        this.centipede.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.centipede.isRearing()) {
            return;
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.lungeCooldown > 0) this.lungeCooldown--;
        if (this.rearCooldown > 0) this.rearCooldown--;
        if (this.repathTimer > 0) this.repathTimer--;

        double distSqr = this.centipede.distanceToSqr(target);
        double reach = this.centipede.getBbWidth() * 2.0 * this.centipede.getBbWidth() * 2.0 + target.getBbWidth();
        double bodyLen = this.centipede.getSegments() * Centipede.SEGMENT_SPACING;
        double engageDist = 7.0 + bodyLen * 0.5;

        if (distSqr <= reach) {
            if (this.attackCooldown <= 0) {
                this.attackCooldown = 20;
                this.centipede.doHurtTarget(target);
            }
            if (this.rearCooldown <= 0 && this.centipede.onGround() && this.centipede.getRandom().nextInt(70) == 0) {
                this.centipede.startRear(40 + this.centipede.getRandom().nextInt(40));
                this.rearCooldown = 220;
                return;
            }
            if (this.repathTimer <= 0) {
                this.repathTimer = 5;
                this.centipede.getNavigation().moveTo(target, 1.0);
            }
            return;
        }

        double dist = Math.sqrt(distSqr);

        if (this.repositionTicks > 0) {
            this.repositionTicks--;
            reposition(target, bodyLen);
            return;
        }

        if (dist <= engageDist) {
            if (this.centipede.onGround() && this.lungeCooldown <= 0 && dist > 2.5 && this.centipede.getRandom().nextInt(50) == 0) {
                lungeTowards(target);
                this.lungeCooldown = 80;
                return;
            }
            if (this.centipede.getRandom().nextInt(110) == 0) {
                this.repositionTicks = 16 + this.centipede.getRandom().nextInt(16);
                this.orbitDir = this.centipede.getRandom().nextBoolean() ? 1 : -1;
                return;
            }
            if (this.repathTimer <= 0) {
                this.repathTimer = 8;
                this.centipede.getNavigation().moveTo(target, 1.0);
            }
            return;
        }

        if (this.repathTimer <= 0) {
            this.repathTimer = 10;
            this.centipede.getNavigation().moveTo(target, 1.1);
        }
    }

    private void reposition(LivingEntity target, double bodyLen) {
        if (this.repathTimer > 0) return;
        this.repathTimer = 6;
        double orbitRadius = Math.max(3.0, bodyLen * 0.7);
        double dx = this.centipede.getX() - target.getX();
        double dz = this.centipede.getZ() - target.getZ();
        double angle = Mth.atan2(dz, dx) + this.orbitDir * 0.35;
        double px = target.getX() + Math.cos(angle) * orbitRadius;
        double pz = target.getZ() + Math.sin(angle) * orbitRadius;
        this.centipede.getNavigation().moveTo(px, target.getY(), pz, 1.0);
    }

    private void lungeTowards(LivingEntity target) {
        Vec3 lead = target.position().add(target.getDeltaMovement().scale(6.0));
        double dx = lead.x - this.centipede.getX();
        double dz = lead.z - this.centipede.getZ();
        double h = Math.sqrt(dx * dx + dz * dz);
        if (h < 1.0E-4) return;
        double ahead = Math.min(3.0, h);
        double lx = this.centipede.getX() + dx / h * ahead;
        double lz = this.centipede.getZ() + dz / h * ahead;
        if (!hasGroundBelow(lx, this.centipede.getY() + 0.5, lz)) return;
        this.centipede.requestLunge(dx, 0.3, dz);
    }

    private boolean hasGroundBelow(double x, double y, double z) {
        Level level = this.centipede.level();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        for (int d = 0; d < 4; d++) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return true;
            pos.move(0, -1, 0);
        }
        return false;
    }
}
