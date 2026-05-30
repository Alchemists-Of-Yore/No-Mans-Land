package com.farcr.nomansland.common.entity.goose;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class GooseCoreBehavior extends Behavior<Goose> {
    private static final double THREAT_RANGE = 3.0;
    private static final int GRUDGE_AMBUSH_CHANCE = 160;
    private static final long ACTIVE_ANGER_TICKS = 20L * 30;
    private static final double WITNESS_RADIUS = 12.0;

    @Nullable
    private UUID lastAlertedAttacker;

    public GooseCoreBehavior() {
        super(Map.of(), Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return true;
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        Brain<Goose> brain = goose.getBrain();
        handleProvocation(goose, brain);

        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return;
        if (goose.isCarrying()) return;
        if (goose.isStealing()) return;
        if (brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) return;

        NearestVisibleLivingEntities visible = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .orElse(NearestVisibleLivingEntities.empty());

        if (goose.canFight() && goose.isAttackReady()) {
            LivingEntity grudge = closestInRange(goose, visible, entity -> goose.getGrudges().holdsGrudgeAgainst(entity.getUUID()));
            if (grudge != null && goose.getRandom().nextInt(GRUDGE_AMBUSH_CHANCE) == 0) {
                commitToAttack(goose, grudge);
                return;
            }
        }

        LivingEntity threat = closestInRange(goose, visible,
                entity -> GooseAI.isThreat(entity) || goose.getGrudges().holdsGrudgeAgainst(entity.getUUID()));
        if (threat != null) {
            standOffAgainst(brain, threat);
        }
    }

    private void handleProvocation(Goose goose, Brain<Goose> brain) {
        brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent(attacker -> {
            if (goose.isCarrying()) goose.dropCarriedItem();

            UUID attackerId = attacker.getUUID();
            holdGrudgeAndAnger(goose, attackerId);

            if (!attackerId.equals(lastAlertedAttacker)) {
                lastAlertedAttacker = attackerId;
                for (Goose witness : goose.nearbyGeese(WITNESS_RADIUS)) {
                    holdGrudgeAndAnger(witness, attackerId);
                }
            }

            if (goose.canFight() && goose.isAttackReady() && brain.getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()) {
                commitToAttack(goose, attacker);
            }
        });
    }

    private static void commitToAttack(Goose goose, LivingEntity target) {
        goose.beginAttack(target);
        goose.rallyFlock(target);
    }

    private static void holdGrudgeAndAnger(Goose goose, UUID attackerId) {
        goose.getGrudges().hold(attackerId);
        goose.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, attackerId, ACTIVE_ANGER_TICKS);
    }

    @Nullable
    private static LivingEntity closestInRange(Goose goose, NearestVisibleLivingEntities visible, Predicate<LivingEntity> predicate) {
        return visible.findClosest(entity ->
                predicate.test(entity)
                        && entity.canBeSeenAsEnemy()
                        && goose.distanceToSqr(entity) < THREAT_RANGE * THREAT_RANGE
                        && goose.getSensing().hasLineOfSight(entity)
        ).orElse(null);
    }

    private static void standOffAgainst(Brain<Goose> brain, LivingEntity threat) {
        brain.setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new EntityTracker(threat, true), 40L);
        brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, threat, 40L);
    }
}
