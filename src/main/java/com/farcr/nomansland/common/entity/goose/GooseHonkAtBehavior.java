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
    private static final double CLOSE_RANGE_SQR = 6.0;
    private static final double PECK_RANGE_SQR = 2.0;
    private static final float APPROACH_SPEED = 1.1F;
    private static final int START_CHANCE = 200;

    @Nullable
    private LivingEntity focus;
    private int honkCooldown;
    private int peckCooldown;

    public GooseHonkAtBehavior() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 60, 140);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isBaby() || goose.isCarrying() || goose.isFlying() || goose.getRandom().nextInt(START_CHANCE) != 0) return false;
        focus = pickVictim(goose);
        return focus != null;
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        honkCooldown = 0;
        peckCooldown = 20;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return focus != null
                && focus.isAlive()
                && !goose.isCarrying()
                && !goose.isFlying()
                && goose.distanceToSqr(focus) < NOTICE_RADIUS * NOTICE_RADIUS
                && goose.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()
                && goose.getBrain().getMemory(MemoryModuleType.AVOID_TARGET).isEmpty();
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (focus == null) return;
        if (honkCooldown > 0) honkCooldown--;
        if (peckCooldown > 0) peckCooldown--;
        goose.getLookControl().setLookAt(focus);
        double distanceSqr = goose.distanceToSqr(focus);

        if (distanceSqr <= CLOSE_RANGE_SQR && honkCooldown == 0) {
            goose.flapBriefly();
            goose.honkAngry();
            honkCooldown = 70 + goose.getRandom().nextInt(60);
        }

        if (distanceSqr <= PECK_RANGE_SQR) {
            goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            goose.faceToward(focus.getX(), focus.getZ());
            if (peckCooldown == 0) {
                goose.peck();
                goose.doHurtTarget(focus);
                peckCooldown = 30 + goose.getRandom().nextInt(30);
            }
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, focus, APPROACH_SPEED, 0);
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
                && !(entity instanceof Monster)
                && entity.attackable()
                && !(entity instanceof Player player && player.isSpectator())
                && goose.distanceToSqr(entity) < NOTICE_RADIUS * NOTICE_RADIUS
        ).forEach(candidates::add);
        return candidates.isEmpty() ? null : candidates.get(goose.getRandom().nextInt(candidates.size()));
    }
}
