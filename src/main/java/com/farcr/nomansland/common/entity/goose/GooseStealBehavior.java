package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GooseStealBehavior extends Behavior<Goose> {
    private static final int START_CHANCE = 120;
    private static final int GRUDGE_STEAL_CHANCE = 40;
    private static final double ITEM_SEARCH_RADIUS = 8.0;
    private static final double HAND_SEARCH_RADIUS = 6.0;
    private static final double TAKE_DISTANCE_SQR = 2.0;
    private static final double GIVE_UP_SQR = 256.0;
    private static final double OBSERVED_CONE = 0.7;
    private static final float APPROACH_SPEED = 1.15F;

    @Nullable private ItemEntity targetItem;
    @Nullable private Player targetPlayer;

    public GooseStealBehavior() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 60, 200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isBaby() || goose.isCarrying()) return false;
        boolean eager = !goose.getGrudges().targets().isEmpty() && goose.getRandom().nextInt(GRUDGE_STEAL_CHANCE) == 0;
        if (!eager && goose.getRandom().nextInt(START_CHANCE) != 0) return false;
        targetItem = findDroppedItem(goose);
        if (targetItem != null) return true;
        targetPlayer = findVictimPlayer(goose);
        return targetPlayer != null;
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        goose.setAnchor(goose.blockPosition());
        goose.setStealing(true);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        if (goose.isCarrying() || goose.isBaby() || goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
        if (targetItem != null) {
            return targetItem.isAlive() && goose.distanceToSqr(targetItem) < GIVE_UP_SQR;
        }
        return targetPlayer != null && targetPlayer.isAlive()
                && isDesirable(targetPlayer.getMainHandItem())
                && goose.distanceToSqr(targetPlayer) < GIVE_UP_SQR;
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (targetItem != null) {
            stealDroppedItem(goose);
        } else if (targetPlayer != null) {
            stealFromHand(goose);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        goose.setStealing(false);
        targetItem = null;
        targetPlayer = null;
    }

    private void stealDroppedItem(Goose goose) {
        goose.getLookControl().setLookAt(targetItem);
        if (goose.distanceToSqr(targetItem) <= TAKE_DISTANCE_SQR) {
            take(goose, targetItem.getItem());
            targetItem.getItem().shrink(1);
            if (targetItem.getItem().isEmpty()) targetItem.discard();
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, targetItem.blockPosition(), APPROACH_SPEED, 0);
        }
    }

    private void stealFromHand(Goose goose) {
        goose.getLookControl().setLookAt(targetPlayer.getEyePosition());
        boolean watched = isObservedBy(goose, targetPlayer);
        if (!watched && goose.distanceToSqr(targetPlayer) <= TAKE_DISTANCE_SQR) {
            ItemStack hand = targetPlayer.getMainHandItem();
            take(goose, hand);
            hand.shrink(1);
            return;
        }
        Vec3 approach = watched ? blindSpot(targetPlayer) : targetPlayer.position();
        BehaviorUtils.setWalkAndLookTargetMemories(goose, BlockPos.containing(approach), APPROACH_SPEED, 0);
    }

    private static Vec3 blindSpot(Player player) {
        Vec3 facing = player.getViewVector(1.0F).multiply(1.0, 0.0, 1.0).normalize();
        return player.position().subtract(facing.scale(2.0));
    }

    private static void take(Goose goose, ItemStack source) {
        goose.setCarriedItem(source.copyWithCount(1));
        goose.getBrain().eraseMemory(MemoryModuleType.AVOID_TARGET);
        goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Nullable
    private static ItemEntity findDroppedItem(Goose goose) {
        ItemEntity closest = null;
        double best = Double.MAX_VALUE;
        for (ItemEntity item : goose.level().getEntitiesOfClass(ItemEntity.class, goose.getBoundingBox().inflate(ITEM_SEARCH_RADIUS))) {
            if (!isDesirable(item.getItem())) continue;
            double distance = goose.distanceToSqr(item);
            if (distance < best) {
                best = distance;
                closest = item;
            }
        }
        return closest;
    }

    @Nullable
    private static Player findVictimPlayer(Goose goose) {
        Player grudged = null, stranger = null;
        double grudgedBest = Double.MAX_VALUE, strangerBest = Double.MAX_VALUE;
        for (Player player : goose.level().getEntitiesOfClass(Player.class, goose.getBoundingBox().inflate(HAND_SEARCH_RADIUS))) {
            if (player.isSpectator() || player.isCreative() || !isDesirable(player.getMainHandItem())) continue;
            double distance = goose.distanceToSqr(player);
            if (goose.getGrudges().holdsGrudgeAgainst(player.getUUID())) {
                if (distance < grudgedBest) {
                    grudgedBest = distance;
                    grudged = player;
                }
            } else if (distance < strangerBest) {
                strangerBest = distance;
                stranger = player;
            }
        }
        return grudged != null ? grudged : stranger;
    }

    private static boolean isDesirable(ItemStack stack) {
        return !stack.isEmpty();
    }

    private static boolean isObservedBy(Goose goose, Player player) {
        Vec3 view = player.getViewVector(1.0F).normalize();
        Vec3 toGoose = new Vec3(goose.getX() - player.getX(), goose.getEyeY() - player.getEyeY(), goose.getZ() - player.getZ());
        double distance = toGoose.length();
        return view.dot(toGoose.normalize()) > 1.0 - OBSERVED_CONE / distance && player.hasLineOfSight(goose);
    }
}
