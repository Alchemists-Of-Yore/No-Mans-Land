package com.farcr.nomansland.common.entity.goose;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GooseSocializeBehavior extends Behavior<Goose> {
    private static final int START_CHANCE = 200;
    private static final double NOTICE_RADIUS = 16.0;
    private static final double FAR_ENOUGH_SQR = 25.0;
    private static final double COMFY_SQR = 9.0;
    private static final float SPEED = 0.9F;

    @Nullable private Goose buddy;

    public GooseSocializeBehavior() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 60, 120);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isBaby() || goose.getRandom().nextInt(START_CHANCE) != 0) return false;
        buddy = pickBuddy(goose);
        return buddy != null;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return buddy != null && buddy.isAlive() && !goose.isCarrying() && !goose.isFlying()
                && goose.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()
                && goose.getBrain().getMemory(MemoryModuleType.AVOID_TARGET).isEmpty()
                && goose.distanceToSqr(buddy) > COMFY_SQR;
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (buddy != null) {
            if (goose.getRandom().nextInt(80) == 0) goose.honkCurious();
            BehaviorUtils.setWalkAndLookTargetMemories(goose, buddy.blockPosition(), SPEED, 3);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        buddy = null;
    }

    @Nullable
    private static Goose pickBuddy(Goose goose) {
        Goose closest = null;
        double best = Double.MAX_VALUE;
        for (Goose other : goose.nearbyGeese(NOTICE_RADIUS)) {
            double distance = goose.distanceToSqr(other);
            if (distance > FAR_ENOUGH_SQR && distance < best) {
                best = distance;
                closest = other;
            }
        }
        return closest;
    }
}
