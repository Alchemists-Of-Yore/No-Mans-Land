package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GooseThreatenBehavior extends Behavior<Goose> {
    private static final int BASE_PATIENCE = 140;
    private static final int CONFIDENCE_BONUS = 10;
    private static final int MIN_PATIENCE = 80;
    private static final int ESCALATION_CHANCE = 60;
    private static final int MAX_STANDOFF_TICKS = 160;
    private static final double CROWDING_RANGE_SQR = 6.25;
    private static final float BACKPEDAL_SPEED = 0.45F;
    private static final double LOST_THREAT_SQR = 25.0;
    private static final float FLEE_SPEED = 1.4F;
    private static final float STRUT_SPEED = 0.9F;
    private static final double ADULT_SEARCH_RADIUS = 16.0;
    private static final double HIDE_BEHIND_PARENT = 1.5;
    private static final double WEAPON_SEARCH_RADIUS = 8.0;
    private static final double WEAPON_GRAB_SQR = 2.0;
    private static final int WEAPON_SEARCH_COOLDOWN = 30;
    private static final int MAX_DETOUR_TICKS = 60;
    private static final float WEAPON_FETCH_SPEED = 1.2F;

    private int threatenTicks;
    private int honkCooldown;
    @Nullable
    private ItemEntity weaponTarget;
    private int weaponSearchCooldown;
    private int detourTicks;

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
        honkCooldown = 10;
        weaponTarget = null;
        weaponSearchCooldown = 15;
        detourTicks = 0;
        goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (goose.isFlying()) return;
        LivingEntity threat = goose.getBrain().getMemory(MemoryModuleType.AVOID_TARGET).orElse(null);
        if (threat == null) return;

        boolean provoked = goose.getLastHurtByMob() == threat;
        if (!threat.isAlive() || goose.distanceToSqr(threat) > LOST_THREAT_SQR || lostInterest(goose, threat, provoked)) {
            disengage(goose, threat);
            return;
        }
        goose.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, threat, 40L);
        goose.getLookControl().setLookAt(threat, 30.0F, 30.0F);

        if (goose.canFight()) {
            if (seekWeapon(goose)) {
                threatenTicks++;
                return;
            }
            menace(goose, threat);
            if (--honkCooldown <= 0) {
                goose.honkAngry();
                honkCooldown = 90 + goose.getRandom().nextInt(60);
            }
            threatenTicks++;
            int patience = Math.max(MIN_PATIENCE, BASE_PATIENCE - CONFIDENCE_BONUS * goose.flockConfidence());
            boolean crowded = threatenTicks >= patience
                    && goose.isAttackReady()
                    && goose.distanceToSqr(threat) < CROWDING_RANGE_SQR
                    && goose.getRandom().nextInt(ESCALATION_CHANCE) == 0;
            if (provoked || crowded) {
                goose.beginAttack(threat);
                goose.rallyFlock(threat);
            } else if (threatenTicks > MAX_STANDOFF_TICKS) {
                disengage(goose, threat);
            }
        } else {
            Goose parent = goose.isBaby() ? nearestAdult(goose) : null;
            if (parent != null) {
                hideBehind(goose, parent, threat);
            } else if (!flee(goose, threat)) {
                menace(goose, threat);
                if (--honkCooldown <= 0) {
                    goose.honkAfraid();
                    honkCooldown = 70 + goose.getRandom().nextInt(40);
                }
            } else if (--honkCooldown <= 0) {
                goose.honkAfraid();
                honkCooldown = 60 + goose.getRandom().nextInt(40);
            }
        }
    }

    private static boolean lostInterest(Goose goose, LivingEntity threat, boolean provoked) {
        return !provoked && threat instanceof Player player && !GooseAI.isFacing(player, goose);
    }

    private void disengage(Goose goose, LivingEntity threat) {
        goose.getBrain().eraseMemory(MemoryModuleType.AVOID_TARGET);
        goose.setAttackCooldown(60);
        if (goose.canFight() && !goose.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
            Vec3 away = DefaultRandomPos.getPosAway(goose, 8, 4, threat.position());
            if (away != null) {
                BehaviorUtils.setWalkAndLookTargetMemories(goose, BlockPos.containing(away), STRUT_SPEED, 0);
            }
        }
    }

    private boolean seekWeapon(Goose goose) {
        if (goose.isCarrying()) return false;
        if (weaponTarget == null) {
            if (--weaponSearchCooldown > 0) return false;
            weaponSearchCooldown = WEAPON_SEARCH_COOLDOWN;
            weaponTarget = goose.findNearbyWeapon(WEAPON_SEARCH_RADIUS);
            if (weaponTarget == null) return false;
            detourTicks = MAX_DETOUR_TICKS;
        }
        if (!weaponTarget.isAlive() || --detourTicks <= 0) {
            weaponTarget = null;
            return false;
        }
        if (goose.distanceToSqr(weaponTarget) <= WEAPON_GRAB_SQR) {
            goose.grabItem(weaponTarget);
            goose.honkAngry();
            goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            weaponTarget = null;
            return false;
        }
        BehaviorUtils.setWalkAndLookTargetMemories(goose, weaponTarget.blockPosition(), WEAPON_FETCH_SPEED, 0);
        return true;
    }

    private static void menace(Goose goose, LivingEntity threat) {
        double dx = threat.getX() - goose.getX();
        double dz = threat.getZ() - goose.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        goose.setYRot(yaw);
        goose.yBodyRot = yaw;
        goose.getMoveControl().strafe(-BACKPEDAL_SPEED, 0.0F);
    }

    private static boolean flee(Goose goose, LivingEntity threat) {
        if (goose.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) return true;
        Vec3 away = DefaultRandomPos.getPosAway(goose, 10, 5, threat.position());
        if (away == null) return false;
        BehaviorUtils.setWalkAndLookTargetMemories(goose, BlockPos.containing(away), FLEE_SPEED, 0);
        return true;
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
