package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GooseStealBehavior extends Behavior<Goose> {
    private static final int START_CHANCE = 120;
    private static final int GRUDGE_STEAL_CHANCE = 40;
    private static final int CONTAINER_CHANCE = 3;
    private static final double ITEM_SEARCH_RADIUS = 8.0;
    private static final double HAND_SEARCH_RADIUS = 6.0;
    private static final double CONTAINER_SEARCH_RADIUS = 10.0;
    private static final double TAKE_DISTANCE_SQR = 2.0;
    private static final double RAID_DISTANCE_SQR = 3.5;
    private static final double GIVE_UP_SQR = 256.0;
    private static final double OBSERVED_CONE = 0.7;
    private static final float APPROACH_SPEED = 1.15F;
    private static final int RAID_PECKS = 3;
    private static final int FRESH_AGE_TICKS = 100;
    private static final int SEASONED_AGE_TICKS = 1200;
    private static final float FRESH_PICK_CHANCE = 0.15F;
    private static final double REACHABLE_HEIGHT = 3.0;

    @Nullable private ItemEntity targetItem;
    @Nullable private Player targetPlayer;
    @Nullable private BlockPos targetContainer;
    private int raidPecks;
    private int raidPeckDelay;

    public GooseStealBehavior() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 60, 300);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isBaby() || goose.isCarrying() || goose.isFlying()) return false;
        boolean eager = !goose.getGrudges().targets().isEmpty() && goose.getRandom().nextInt(GRUDGE_STEAL_CHANCE) == 0;
        if (!eager && goose.getRandom().nextInt(START_CHANCE) != 0) return false;
        targetItem = findDroppedItem(goose);
        if (targetItem != null) return true;
        targetPlayer = findVictimPlayer(goose);
        if (targetPlayer != null) return true;
        if (goose.getRandom().nextInt(CONTAINER_CHANCE) == 0) {
            targetContainer = findContainer(level, goose);
        }
        return targetContainer != null;
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        goose.setAnchor(goose.blockPosition());
        goose.setStealing(true);
        raidPecks = 0;
        raidPeckDelay = 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        if (goose.isCarrying() || goose.isBaby() || goose.isFlying()
                || goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
        if (targetItem != null) {
            return targetItem.isAlive() && goose.distanceToSqr(targetItem) < GIVE_UP_SQR;
        }
        if (targetPlayer != null) {
            return targetPlayer.isAlive()
                    && !targetPlayer.isCreative()
                    && !targetPlayer.isSpectator()
                    && isDesirable(targetPlayer.getMainHandItem())
                    && Math.abs(targetPlayer.getY() - goose.getY()) <= REACHABLE_HEIGHT
                    && goose.distanceToSqr(targetPlayer) < GIVE_UP_SQR;
        }
        return targetContainer != null
                && goose.distanceToSqr(Vec3.atCenterOf(targetContainer)) < GIVE_UP_SQR
                && lootableContainer(level, targetContainer) != null;
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (targetItem != null) {
            stealDroppedItem(goose);
        } else if (targetPlayer != null) {
            stealFromHand(goose);
        } else if (targetContainer != null) {
            raidContainer(level, goose);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        if (targetContainer != null && raidPecks > 0) {
            setContainerOpen(level, targetContainer, false);
        }
        goose.setStealing(false);
        targetItem = null;
        targetPlayer = null;
        targetContainer = null;
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
            goose.honk();
            return;
        }
        Vec3 approach = watched ? blindSpot(targetPlayer) : targetPlayer.position();
        BehaviorUtils.setWalkAndLookTargetMemories(goose, BlockPos.containing(approach), APPROACH_SPEED, 0);
    }

    private void raidContainer(ServerLevel level, Goose goose) {
        Vec3 center = Vec3.atCenterOf(targetContainer);
        goose.getLookControl().setLookAt(center.x, center.y, center.z);
        if (goose.distanceToSqr(center) > RAID_DISTANCE_SQR) {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, targetContainer, APPROACH_SPEED, 1);
            return;
        }

        goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        if (--raidPeckDelay > 0) return;
        raidPeckDelay = 12 + goose.getRandom().nextInt(8);

        Container container = lootableContainer(level, targetContainer);
        if (container == null) return;

        goose.peck();
        if (++raidPecks == 1) {
            setContainerOpen(level, targetContainer, true);
        }
        if (raidPecks < RAID_PECKS) return;

        int slot = randomFilledSlot(container, goose);
        if (slot >= 0) {
            take(goose, container.removeItem(slot, 1));
            container.setChanged();
        }
        setContainerOpen(level, targetContainer, false);
        goose.honk();
        targetContainer = null;
    }

    private static void setContainerOpen(ServerLevel level, BlockPos pos, boolean open) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;
        if (state.hasProperty(BlockStateProperties.OPEN)) {
            if (state.getValue(BlockStateProperties.OPEN) != open) {
                level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, open), 3);
            }
            level.playSound(null, pos, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.5F, 1.1F);
        } else {
            level.blockEvent(pos, state.getBlock(), 1, open ? 1 : 0);
            level.playSound(null, pos, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 1.1F);
        }
    }

    private static int randomFilledSlot(Container container, Goose goose) {
        int filled = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty()) filled++;
        }
        if (filled == 0) return -1;
        int pick = goose.getRandom().nextInt(filled);
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty() && pick-- == 0) return slot;
        }
        return -1;
    }

    @Nullable
    private static Container lootableContainer(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof Container container)) return null;
        return container.isEmpty() ? null : container;
    }

    @Nullable
    private static BlockPos findContainer(ServerLevel level, Goose goose) {
        BlockPos origin = goose.blockPosition();
        ChunkPos chunkPos = goose.chunkPosition();
        BlockPos closest = null;
        double best = Double.MAX_VALUE;
        for (int chunkX = chunkPos.x - 1; chunkX <= chunkPos.x + 1; chunkX++) {
            for (int chunkZ = chunkPos.z - 1; chunkZ <= chunkPos.z + 1; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) continue;
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    if (!(entry.getValue() instanceof Container container) || container.isEmpty()) continue;
                    double distance = origin.distSqr(pos);
                    if (distance < CONTAINER_SEARCH_RADIUS * CONTAINER_SEARCH_RADIUS
                            && Math.abs(pos.getY() - origin.getY()) <= 4
                            && distance < best) {
                        best = distance;
                        closest = pos.immutable();
                    }
                }
            }
        }
        return closest;
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
            if (goose.getRandom().nextFloat() > pickChance(item)) continue;
            double distance = goose.distanceToSqr(item);
            if (distance < best) {
                best = distance;
                closest = item;
            }
        }
        return closest;
    }

    private static float pickChance(ItemEntity item) {
        float seasoning = Mth.clamp((item.getAge() - FRESH_AGE_TICKS) / (float) (SEASONED_AGE_TICKS - FRESH_AGE_TICKS), 0.0F, 1.0F);
        return FRESH_PICK_CHANCE + (1.0F - FRESH_PICK_CHANCE) * seasoning;
    }

    @Nullable
    private static Player findVictimPlayer(Goose goose) {
        Player grudged = null, stranger = null;
        double grudgedBest = Double.MAX_VALUE, strangerBest = Double.MAX_VALUE;
        for (Player player : goose.level().getEntitiesOfClass(Player.class, goose.getBoundingBox().inflate(HAND_SEARCH_RADIUS))) {
            if (player.isSpectator() || player.isCreative() || !isDesirable(player.getMainHandItem())) continue;
            if (Math.abs(player.getY() - goose.getY()) > REACHABLE_HEIGHT) continue;
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
