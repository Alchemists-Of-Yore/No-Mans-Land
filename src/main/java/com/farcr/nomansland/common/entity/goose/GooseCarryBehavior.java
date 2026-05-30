package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GooseCarryBehavior extends Behavior<Goose> {
    private enum Plan { EAT, SHARE, WIELD, STASH }

    private static final float FLEE_SPEED = 1.5F;
    private static final float APPROACH_SPEED = 1.25F;
    private static final double REACHED_SQR = 4.0;
    private static final double FLOCK_RADIUS = 12.0;
    private static final double VICTIM_RADIUS_SQR = 100.0;
    private static final int WATER_SCAN_RADIUS = 10;
    private static final int WIELD_DURATION = 80;
    private static final int PECK_COOLDOWN = 20;
    private static final int MAX_INTERACT_TICKS = 200;

    private Plan plan = Plan.STASH;
    private int fleeTicks;
    private int interactTicks;
    private boolean fleeing;
    @Nullable private BlockPos fleeTarget;
    @Nullable private LivingEntity wieldVictim;
    @Nullable private Goose shareTarget;
    private int wieldTicks;
    private int peckCooldown;

    public GooseCarryBehavior() {
        super(Map.of(), Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        return goose.isCarrying();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return goose.isCarrying();
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        fleeing = true;
        fleeTicks = 50 + goose.getRandom().nextInt(50);
        interactTicks = MAX_INTERACT_TICKS;
        fleeTarget = findWater(goose);
        plan = choosePlan(goose);
        wieldVictim = null;
        shareTarget = null;
        wieldTicks = WIELD_DURATION;
        peckCooldown = 0;
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (peckCooldown > 0) peckCooldown--;

        if (fleeing) {
            flee(goose);
            if (--fleeTicks <= 0) fleeing = false;
            return;
        }

        if (--interactTicks <= 0) {
            goose.dropCarriedItem();
            return;
        }

        switch (plan) {
            case EAT -> eat(goose);
            case SHARE -> share(goose);
            case WIELD -> wield(goose);
            case STASH -> stash(goose);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        fleeTarget = null;
        wieldVictim = null;
        shareTarget = null;
        goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private void flee(Goose goose) {
        if (fleeTarget != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, fleeTarget, FLEE_SPEED, 0);
            if (goose.blockPosition().distSqr(fleeTarget) <= REACHED_SQR) fleeing = false;
            return;
        }
        Player player = goose.level().getNearestPlayer(goose, 16.0);
        if (player != null) {
            Vec3 away = goose.position().subtract(player.position()).normalize().scale(8.0).add(goose.position());
            BehaviorUtils.setWalkAndLookTargetMemories(goose, BlockPos.containing(away), FLEE_SPEED, 0);
        }
    }

    private void eat(Goose goose) {
        if (goose.isFood(goose.getCarriedItem())) {
            goose.setCarriedItem(ItemStack.EMPTY);
        } else {
            stash(goose);
        }
    }

    private void share(Goose goose) {
        if (shareTarget == null || !shareTarget.isAlive() || shareTarget.isCarrying()) {
            shareTarget = nearestFreeFlockmate(goose);
        }
        if (shareTarget == null) {
            stash(goose);
            return;
        }
        goose.getLookControl().setLookAt(shareTarget);
        if (goose.distanceToSqr(shareTarget) <= REACHED_SQR) {
            shareTarget.setCarriedItem(goose.getCarriedItem());
            goose.setCarriedItem(ItemStack.EMPTY);
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, shareTarget.blockPosition(), APPROACH_SPEED, 1);
        }
    }

    private void wield(Goose goose) {
        if (wieldVictim == null || !wieldVictim.isAlive()) {
            wieldVictim = randomVictim(goose);
        }
        if (wieldVictim == null || wieldTicks-- <= 0) {
            goose.dropCarriedItem();
            return;
        }
        BehaviorUtils.setWalkAndLookTargetMemories(goose, wieldVictim, APPROACH_SPEED, 0);
        if (peckCooldown == 0 && goose.isWithinMeleeAttackRange(wieldVictim)) {
            goose.swing(InteractionHand.MAIN_HAND);
            goose.doHurtTarget(wieldVictim);
            peckCooldown = PECK_COOLDOWN;
        }
    }

    private void stash(Goose goose) {
        BlockPos anchor = goose.getAggressionAnchor();
        if (anchor == null || goose.blockPosition().distSqr(anchor) <= REACHED_SQR) {
            goose.dropCarriedItem();
            return;
        }
        BehaviorUtils.setWalkAndLookTargetMemories(goose, anchor, APPROACH_SPEED, 1);
    }

    private Plan choosePlan(Goose goose) {
        boolean edible = goose.isFood(goose.getCarriedItem());
        boolean hasFlockmate = nearestFreeFlockmate(goose) != null;
        int roll = goose.getRandom().nextInt(100);
        if (edible && roll < 30) return Plan.EAT;
        if (hasFlockmate && roll < 55) return Plan.SHARE;
        if (roll < 70) return Plan.WIELD;
        return Plan.STASH;
    }

    @Nullable
    private static Goose nearestFreeFlockmate(Goose goose) {
        Goose closest = null;
        double best = Double.MAX_VALUE;
        for (Goose other : goose.nearbyGeese(FLOCK_RADIUS)) {
            if (other.isBaby() || other.isCarrying()) continue;
            double distance = goose.distanceToSqr(other);
            if (distance < best) {
                best = distance;
                closest = other;
            }
        }
        return closest;
    }

    @Nullable
    private static LivingEntity randomVictim(Goose goose) {
        NearestVisibleLivingEntities visible = goose.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .orElse(NearestVisibleLivingEntities.empty());
        List<LivingEntity> candidates = new ArrayList<>();
        visible.findAll(entity -> entity != goose && !(entity instanceof Goose) && goose.distanceToSqr(entity) < VICTIM_RADIUS_SQR)
                .forEach(candidates::add);
        return candidates.isEmpty() ? null : candidates.get(goose.getRandom().nextInt(candidates.size()));
    }

    @Nullable
    private static BlockPos findWater(Goose goose) {
        BlockPos origin = goose.blockPosition();
        BlockPos closest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-WATER_SCAN_RADIUS, -2, -WATER_SCAN_RADIUS),
                origin.offset(WATER_SCAN_RADIUS, 2, WATER_SCAN_RADIUS))) {
            if (!goose.level().getFluidState(pos).is(FluidTags.WATER)) continue;
            double distance = origin.distSqr(pos);
            if (distance < best) {
                best = distance;
                closest = pos.immutable();
            }
        }
        return closest;
    }
}
