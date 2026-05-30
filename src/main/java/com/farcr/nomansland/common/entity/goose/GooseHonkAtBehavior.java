package com.farcr.nomansland.common.entity.goose;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GooseHonkAtBehavior extends Behavior<Goose> {
    private static final double NOTICE_RADIUS = 12.0;
    private static final double HONK_DISTANCE_SQR = 6.0;
    private static final float APPROACH_SPEED = 1.1F;
    private static final int START_CHANCE = 200;

    @Nullable
    private LivingEntity focus;

    public GooseHonkAtBehavior() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 40, 100);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isBaby() || goose.getRandom().nextInt(START_CHANCE) != 0) return false;
        focus = pickVictim(goose);
        return focus != null;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return focus != null
                && focus.isAlive()
                && goose.distanceToSqr(focus) < NOTICE_RADIUS * NOTICE_RADIUS
                && goose.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()
                && goose.getBrain().getMemory(MemoryModuleType.AVOID_TARGET).isEmpty();
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (focus == null) return;
        goose.getLookControl().setLookAt(focus);
        if (goose.distanceToSqr(focus) <= HONK_DISTANCE_SQR) {
            goose.flapBriefly();
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, focus, APPROACH_SPEED, 1);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        focus = null;
    }

    @Nullable
    private LivingEntity pickVictim(Goose goose) {
        NearestVisibleLivingEntities visible = goose.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .orElse(NearestVisibleLivingEntities.empty());
        List<LivingEntity> candidates = new ArrayList<>();
        visible.findAll(entity -> entity != goose
                && !(entity instanceof Goose)
                && !(entity instanceof Player)
                && !(entity instanceof Monster)
                && goose.distanceToSqr(entity) < NOTICE_RADIUS * NOTICE_RADIUS
        ).forEach(candidates::add);
        return candidates.isEmpty() ? null : candidates.get(goose.getRandom().nextInt(candidates.size()));
    }
}
