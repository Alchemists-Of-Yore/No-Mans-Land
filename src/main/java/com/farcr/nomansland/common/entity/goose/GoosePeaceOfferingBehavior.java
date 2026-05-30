package com.farcr.nomansland.common.entity.goose;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GoosePeaceOfferingBehavior extends Behavior<Goose> {
    private static final double SEARCH_RADIUS = 8.0;
    private static final double EAT_DISTANCE_SQR = 2.25;
    private static final float APPROACH_SPEED = 1.1F;

    @Nullable
    private ItemEntity offering;

    public GoosePeaceOfferingBehavior() {
        super(Map.of(), 40, 200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.getGrudges().targets().isEmpty() || isBusy(goose)) return false;
        offering = findOffering(goose);
        return offering != null;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return offering != null
                && offering.isAlive()
                && !goose.getGrudges().targets().isEmpty()
                && !isBusy(goose);
    }

    private static boolean isBusy(Goose goose) {
        return goose.isCarrying()
                || goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                || goose.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET);
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (offering == null) return;
        goose.getLookControl().setLookAt(offering);
        if (goose.distanceToSqr(offering) <= EAT_DISTANCE_SQR) {
            acceptOffering(goose);
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, offering.blockPosition(), APPROACH_SPEED, 0);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        offering = null;
    }

    @Nullable
    private ItemEntity findOffering(Goose goose) {
        for (ItemEntity item : goose.level().getEntitiesOfClass(ItemEntity.class, goose.getBoundingBox().inflate(SEARCH_RADIUS))) {
            Entity thrower = item.getOwner();
            if (thrower != null
                    && goose.getGrudges().holdsGrudgeAgainst(thrower.getUUID())
                    && goose.isFood(item.getItem())) {
                return item;
            }
        }
        return null;
    }

    private void acceptOffering(Goose goose) {
        if (offering == null) return;
        Entity thrower = offering.getOwner();
        ItemStack stack = offering.getItem();
        stack.shrink(1);
        if (stack.isEmpty()) offering.discard();

        if (thrower != null && goose.getGrudges().offerPeace(thrower.getUUID(), goose.getRandom())) {
            goose.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        }
        offering = null;
    }
}
