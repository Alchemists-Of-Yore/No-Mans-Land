package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GooseCarryBehavior extends Behavior<Goose> {
    private enum Plan { EAT, SHARE, WIELD, HORN, STASH }

    private static final float FLEE_SPEED = 1.5F;
    private static final float APPROACH_SPEED = 1.25F;
    private static final double REACHED_SQR = 4.0;
    private static final double FLOCK_RADIUS = 12.0;
    private static final double VICTIM_RADIUS_SQR = 100.0;
    private static final int WATER_SCAN_RADIUS = 10;
    private static final int WIELD_DURATION = 80;
    private static final int PECK_COOLDOWN = 20;
    private static final int MAX_INTERACT_TICKS = 200;
    private static final float MAX_WEAPON_BONUS = 5.0F;
    private static final int BITES_TO_FINISH = 3;
    private static final int HORN_BLOWS = 3;

    private Plan plan = Plan.STASH;
    private int fleeTicks;
    private int interactTicks;
    private boolean fleeing;
    @Nullable private BlockPos fleeTarget;
    @Nullable private LivingEntity victim;
    @Nullable private Goose shareTarget;
    private int wieldTicks;
    private int peckCooldown;
    private int eatDelay;
    private int bites;
    private int hornBlows;
    private int hornCooldown;

    public GooseCarryBehavior() {
        super(Map.of(), Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        return goose.isCarrying() && !inCombat(goose);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return goose.isCarrying() && !inCombat(goose);
    }

    private static boolean inCombat(Goose goose) {
        return goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                || goose.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET);
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        if (goose.getAggressionAnchor() == null) goose.setAnchor(goose.blockPosition());
        fleeing = true;
        fleeTicks = 50 + goose.getRandom().nextInt(50);
        interactTicks = MAX_INTERACT_TICKS;
        fleeTarget = findWater(goose);
        plan = choosePlan(goose);
        victim = null;
        shareTarget = null;
        wieldTicks = WIELD_DURATION;
        peckCooldown = 0;
        eatDelay = 8;
        bites = 0;
        hornBlows = 0;
        hornCooldown = 30;
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (goose.isFlying()) return;
        if (peckCooldown > 0) peckCooldown--;

        if (fleeing) {
            flee(goose);
            if (--fleeTicks <= 0) endFlee(goose);
            return;
        }

        if (--interactTicks <= 0) {
            goose.dropCarriedItem();
            return;
        }

        switch (plan) {
            case EAT -> eat(goose);
            case SHARE -> share(goose);
            case WIELD -> wield(level, goose);
            case HORN -> blowHorn(level, goose);
            case STASH -> stash(goose);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        fleeTarget = null;
        victim = null;
        shareTarget = null;
        goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private void flee(Goose goose) {
        if (fleeTarget != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, fleeTarget, FLEE_SPEED, 0);
            if (goose.blockPosition().distSqr(fleeTarget) <= REACHED_SQR) endFlee(goose);
            return;
        }
        if (goose.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) return;
        Player player = goose.level().getNearestPlayer(goose.getX(), goose.getY(), goose.getZ(), 16.0,
                entity -> entity instanceof Player other && !other.isCreative() && !other.isSpectator());
        if (player == null) return;
        Vec3 away = DefaultRandomPos.getPosAway(goose, 10, 4, player.position());
        if (away != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, BlockPos.containing(away), FLEE_SPEED, 0);
        }
    }

    private void endFlee(Goose goose) {
        if (!fleeing) return;
        fleeing = false;
        goose.honk();
    }

    private void eat(Goose goose) {
        ItemStack carried = goose.getCarriedItem();
        if (!isEdible(goose, carried)) {
            stash(goose);
            return;
        }
        if (--eatDelay > 0) return;
        eatDelay = 12;
        goose.peck();
        goose.playSound(SoundEvents.GENERIC_EAT, 0.5F, 1.2F + goose.getRandom().nextFloat() * 0.4F);
        if (++bites >= BITES_TO_FINISH) {
            goose.heal(2.0F);
            goose.setCarriedItem(ItemStack.EMPTY);
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
            shareTarget.setCarriedItem(goose.getCarriedItem().copy());
            goose.setCarriedItem(ItemStack.EMPTY);
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, shareTarget.blockPosition(), APPROACH_SPEED, 1);
        }
    }

    private void wield(ServerLevel level, Goose goose) {
        if (victim == null || !victim.isAlive()) {
            victim = randomVictim(goose);
        }
        if (victim == null || wieldTicks-- <= 0) {
            goose.dropCarriedItem();
            return;
        }
        BehaviorUtils.setWalkAndLookTargetMemories(goose, victim, APPROACH_SPEED, 0);
        if (peckCooldown == 0 && goose.isWithinMeleeAttackRange(victim)) {
            goose.peck();
            float damage = (float) goose.getAttributeValue(Attributes.ATTACK_DAMAGE)
                    + Mth.clamp(Goose.weaponBonus(goose.getCarriedItem()), 0, MAX_WEAPON_BONUS);
            victim.hurt(level.damageSources().mobAttack(goose), damage);
            peckCooldown = PECK_COOLDOWN;
        }
    }

    private void blowHorn(ServerLevel level, Goose goose) {
        Holder<Instrument> instrument = goose.getCarriedItem().get(DataComponents.INSTRUMENT);
        if (instrument == null) {
            stash(goose);
            return;
        }
        if (--hornCooldown > 0) return;
        hornCooldown = 60 + goose.getRandom().nextInt(40);
        level.playSound(null, goose.getX(), goose.getY(), goose.getZ(),
                instrument.value().soundEvent().value(), SoundSource.RECORDS, instrument.value().range() / 16.0F, 1.0F);
        goose.flapBriefly();
        if (++hornBlows >= HORN_BLOWS) plan = Plan.STASH;
    }

    private void stash(Goose goose) {
        BlockPos spot = fleeTarget != null ? fleeTarget : goose.getAggressionAnchor();
        if (spot == null || goose.blockPosition().distSqr(spot) <= REACHED_SQR) {
            goose.dropCarriedItem();
            return;
        }
        BehaviorUtils.setWalkAndLookTargetMemories(goose, spot, APPROACH_SPEED, 1);
    }

    private Plan choosePlan(Goose goose) {
        ItemStack carried = goose.getCarriedItem();
        if (carried.get(DataComponents.INSTRUMENT) != null) return Plan.HORN;
        boolean edible = isEdible(goose, carried);
        boolean armed = Goose.weaponBonus(carried) > 0;
        boolean hasFlockmate = nearestFreeFlockmate(goose) != null;
        int roll = goose.getRandom().nextInt(100);
        if (edible && roll < 40) return Plan.EAT;
        if (armed && roll < 70) return Plan.WIELD;
        if (hasFlockmate && roll < 55) return Plan.SHARE;
        if (roll < 70) return Plan.WIELD;
        return Plan.STASH;
    }

    private static boolean isEdible(Goose goose, ItemStack stack) {
        return stack.has(DataComponents.FOOD) || goose.isFood(stack);
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
        visible.findAll(entity -> entity != goose
                && !(entity instanceof Goose)
                && entity.attackable()
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()))
                && goose.distanceToSqr(entity) < VICTIM_RADIUS_SQR
        ).forEach(candidates::add);
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
