package com.farcr.nomansland.common.entity.goose;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

import java.util.Map;
import java.util.UUID;

public class GooseCoreBehavior extends Behavior<Goose> {

    public GooseCoreBehavior() {
        super(Map.of());
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        Brain<Goose> brain = goose.getBrain();
        NearestVisibleLivingEntities entities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .orElse(NearestVisibleLivingEntities.empty());

        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);

        if (target != null && brain.getMemory(MemoryModuleType.WALK_TARGET).isPresent()) {
            if (!goose.canFight()) {
                brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, target, 20*4);
                clearCombatMemory(brain, goose);
                goose.setState(Goose.State.IDLING);
            } else goose.setState(Goose.State.RUNNING);
        } else {
            brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent(hurtBy -> {
                brain.setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, hurtBy.getUUID(), 20 * 60);
            });

            LivingEntity threat = entities.findClosest(entity ->
                    GooseAI.isThreat(entity) && goose.distanceToSqr(entity) < 9).orElse(null);

            if (threat != null) {
                UUID angryAt = brain.getMemory(MemoryModuleType.ANGRY_AT).orElse(null);

                if (threat.getUUID().equals(angryAt)) {
                    if (goose.canFight()) {
                        brain.setMemory(MemoryModuleType.ATTACK_TARGET, threat);
                        goose.setTarget(threat);
                        brain.eraseMemory(MemoryModuleType.AVOID_TARGET);
                    } else {
                        brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, threat, 20*4);
                        clearCombatMemory(brain, goose);
                        intimidatedBy(brain, goose, threat);
                    }
                } else intimidatedBy(brain, goose, threat);
            } else goose.setState(Goose.State.IDLING);
        }
    }

    private static void intimidatedBy(Brain<Goose> brain, Goose goose, LivingEntity entity) {
        goose.setState(Goose.State.INTIMIDATING);
        brain.setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new EntityTracker(entity, true), 20 * 3);
        brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, entity, 20);
        brain.setMemoryWithExpiry(MemoryModuleType.IS_PANICKING, true, 20 * 3);
        goose.getMoveControl().strafe(-0.3F, 0.0F);
        goose.setYRot(Mth.rotateIfNecessary(goose.getYRot(), goose.yHeadRot, 0));
    }

    private static void clearCombatMemory(Brain<Goose> brain, Goose goose) {
        brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.PATH);
        goose.setTarget(null);
    }
}
