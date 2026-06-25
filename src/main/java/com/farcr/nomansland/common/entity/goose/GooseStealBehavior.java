package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    private static final int PUMPKIN_STEAL_CHANCE = 12;
    private static final int CONTAINER_CHANCE = 3;
    private static final double ITEM_SEARCH_RADIUS = 8.0;
    private static final double HAND_SEARCH_RADIUS = 6.0;
    private static final double CONTAINER_SEARCH_RADIUS = 10.0;
    private static final double RESCUE_RADIUS = 12.0;
    private static final double PLAYER_NEARBY_RADIUS = 32.0;
    private static final int DESPAWN_AGE = 5400;
    private static final double TAKE_DISTANCE_SQR = 2.0;
    private static final double RAID_DISTANCE_SQR = 1.8;
    private static final double RAID_APPROACH_SQR = 3.5;
    private static final double GIVE_UP_SQR = 256.0;
    private static final double OBSERVED_CONE = 0.7;
    private static final float APPROACH_SPEED = 1.15F;
    private static final int RAID_PECKS = 3;
    private static final float RUMMAGE_MISS_CHANCE = 0.15F;
    private static final int FRESH_AGE_TICKS = 100;
    private static final int SEASONED_AGE_TICKS = 1200;
    private static final float FRESH_PICK_CHANCE = 0.15F;
    private static final double REACHABLE_HEIGHT = 3.0;

    @Nullable private ItemEntity targetItem;
    @Nullable private Player targetPlayer;
    private InteractionHand targetHand = InteractionHand.MAIN_HAND;
    @Nullable private BlockPos targetContainer;
    private int raidPecks;
    private int raidPeckDelay;

    public GooseStealBehavior() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 60, 300);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isBaby() || goose.isCarrying() || goose.isFlying()) return false;

        targetItem = findRescueItem(goose);
        if (targetItem != null) return true;

        if (!playerNearby(goose)) return false;

        boolean eager = (!goose.getGrudges().targets().isEmpty() && goose.getRandom().nextInt(GRUDGE_STEAL_CHANCE) == 0)
                || (pumpkinNearby(goose) && goose.getRandom().nextInt(PUMPKIN_STEAL_CHANCE) == 0);
        if (!eager && goose.getRandom().nextInt(START_CHANCE) != 0) return false;

        targetItem = findDroppedItem(goose);
        if (targetItem != null) return true;
        targetPlayer = findVictimPlayer(goose);
        if (targetPlayer != null) {
            targetHand = pickHand(goose, targetPlayer);
            return true;
        }
        if (goose.getRandom().nextInt(CONTAINER_CHANCE) == 0) {
            targetContainer = findContainer(level, goose);
        }
        return targetContainer != null;
    }

    private static boolean playerNearby(Goose goose) {
        return goose.level().hasNearbyAlivePlayer(goose.getX(), goose.getY(), goose.getZ(), PLAYER_NEARBY_RADIUS);
    }

    private static boolean pumpkinNearby(Goose goose) {
        for (ItemEntity item : goose.level().getEntitiesOfClass(ItemEntity.class, goose.getBoundingBox().inflate(ITEM_SEARCH_RADIUS))) {
            if (item.getItem().is(Items.PUMPKIN_SEEDS)) return true;
        }
        for (Player player : goose.level().getEntitiesOfClass(Player.class, goose.getBoundingBox().inflate(HAND_SEARCH_RADIUS))) {
            if (player.getMainHandItem().is(Items.PUMPKIN_SEEDS) || player.getOffhandItem().is(Items.PUMPKIN_SEEDS)) return true;
        }
        return false;
    }

    @Nullable
    private static ItemEntity findRescueItem(Goose goose) {
        ItemEntity best = null;
        double bestScore = -Double.MAX_VALUE;
        for (ItemEntity item : goose.level().getEntitiesOfClass(ItemEntity.class, goose.getBoundingBox().inflate(RESCUE_RADIUS))) {
            if (!isDesirable(item.getItem())) continue;
            boolean owned = item.getPersistentData().getBoolean("GooseDropped");
            boolean nearDespawn = item.getAge() >= DESPAWN_AGE;
            if (!owned && !nearDespawn) continue;
            if (owned && !nearDespawn && goose.getRandom().nextInt(20) != 0) continue;
            double score = (owned ? 1.0E6 : 0.0) + (nearDespawn ? 1.0E5 : 0.0) - goose.distanceToSqr(item);
            if (score > bestScore) {
                bestScore = score;
                best = item;
            }
        }
        return best;
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        goose.setAnchor(goose.blockPosition());
        goose.setStealing(true);
        raidPecks = 0;
        raidPeckDelay = 0;
        goose.honkCurious();
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
                    && isDesirable(targetPlayer.getItemInHand(targetHand))
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
        goose.setRummaging(false);
        targetItem = null;
        targetPlayer = null;
        targetContainer = null;
    }

    private void stealDroppedItem(Goose goose) {
        goose.getLookControl().setLookAt(targetItem);
        if (goose.distanceToSqr(targetItem) <= TAKE_DISTANCE_SQR) {
            goose.peck();
            take(goose, targetItem.getItem());
            targetItem.getItem().shrink(1);
            if (targetItem.getItem().isEmpty()) targetItem.discard();
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, targetItem.blockPosition(), APPROACH_SPEED, 0);
        }
    }

    private void stealFromHand(Goose goose) {
        ItemStack handStack = targetPlayer.getItemInHand(targetHand);
        if (!isDesirable(handStack)) {
            targetPlayer = null;
            return;
        }
        Vec3 handPos = handPosition(targetPlayer, targetHand);
        goose.getLookControl().setLookAt(handPos.x, handPos.y, handPos.z);
        boolean watched = isObservedBy(goose, targetPlayer);
        if (!watched && goose.distanceToSqr(targetPlayer) <= TAKE_DISTANCE_SQR) {
            goose.faceToward(targetPlayer.getX(), targetPlayer.getZ());
            goose.playGrabAnimation();
            take(goose, handStack);
            handStack.shrink(1);
            goose.honk();
            return;
        }
        Vec3 approach = behindToward(targetPlayer, targetHand);
        BehaviorUtils.setWalkAndLookTargetMemories(goose, BlockPos.containing(approach), APPROACH_SPEED, 0);
    }

    private static InteractionHand pickHand(Goose goose, Player player) {
        boolean main = isDesirable(player.getMainHandItem());
        boolean off = isDesirable(player.getOffhandItem());
        if (main && off) return goose.getRandom().nextBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private static HumanoidArm armFor(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
    }

    private static Vec3 rightOf(Player player) {
        Vec3 view = player.getViewVector(1.0F).multiply(1.0, 0.0, 1.0);
        view = view.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : view.normalize();
        return new Vec3(-view.z, 0.0, view.x);
    }

    private static Vec3 handPosition(Player player, InteractionHand hand) {
        double side = armFor(player, hand) == HumanoidArm.RIGHT ? 1.0 : -1.0;
        return player.position().add(rightOf(player).scale(0.45 * side)).add(0.0, player.getBbHeight() * 0.6, 0.0);
    }

    private static Vec3 behindToward(Player player, InteractionHand hand) {
        Vec3 view = player.getViewVector(1.0F).multiply(1.0, 0.0, 1.0);
        view = view.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : view.normalize();
        double side = armFor(player, hand) == HumanoidArm.RIGHT ? 1.0 : -1.0;
        return player.position().subtract(view.scale(1.2)).add(rightOf(player).scale(0.6 * side));
    }

    private void raidContainer(ServerLevel level, Goose goose) {
        Vec3 center = Vec3.atCenterOf(targetContainer);
        double distSqr = goose.distanceToSqr(center);
        double reach = goose.isRummaging() ? RAID_APPROACH_SQR : RAID_DISTANCE_SQR;
        if (distSqr > reach) {
            goose.setRummaging(false);
            goose.getLookControl().setLookAt(center.x, center.y, center.z);
            BehaviorUtils.setWalkAndLookTargetMemories(goose, targetContainer, APPROACH_SPEED, 0);
            return;
        }

        goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        faceContainer(goose, center);
        goose.setRummaging(true);
        if (--raidPeckDelay > 0) return;
        raidPeckDelay = 12 + goose.getRandom().nextInt(8);

        Container container = lootableContainer(level, targetContainer);
        if (container == null) return;

        if (++raidPecks == 1) {
            setContainerOpen(level, targetContainer, true);
        }
        if (raidPecks < RAID_PECKS) return;

        if (goose.getRandom().nextFloat() >= RUMMAGE_MISS_CHANCE) {
            int slot = randomFilledSlot(container, goose);
            if (slot >= 0) {
                take(goose, container.removeItem(slot, 1));
                container.setChanged();
            }
        }
        setContainerOpen(level, targetContainer, false);
        goose.setRummaging(false);
        goose.honk();
        targetContainer = null;
    }

    private static void faceContainer(Goose goose, Vec3 center) {
        goose.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        goose.getLookControl().setLookAt(center.x, center.y, center.z);
        goose.faceToward(center.x, center.z);
        Vec3 motion = goose.getDeltaMovement();
        goose.setDeltaMovement(0.0, motion.y, 0.0);
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

    private static void take(Goose goose, ItemStack source) {
        goose.setCarriedItem(source.copyWithCount(1));
        goose.getBrain().eraseMemory(MemoryModuleType.AVOID_TARGET);
        goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Nullable
    private static ItemEntity findDroppedItem(Goose goose) {
        ItemEntity best = null;
        double bestScore = -Double.MAX_VALUE;
        for (ItemEntity item : goose.level().getEntitiesOfClass(ItemEntity.class, goose.getBoundingBox().inflate(ITEM_SEARCH_RADIUS))) {
            if (!isDesirable(item.getItem())) continue;
            boolean pumpkin = item.getItem().is(Items.PUMPKIN_SEEDS);
            float chance = pumpkin ? 1.0F : pickChance(item);
            if (goose.getRandom().nextFloat() > chance) continue;
            double score = (pumpkin ? 1.0E6 : 0.0) - goose.distanceToSqr(item);
            if (score > bestScore) {
                bestScore = score;
                best = item;
            }
        }
        return best;
    }

    private static float pickChance(ItemEntity item) {
        float seasoning = Mth.clamp((item.getAge() - FRESH_AGE_TICKS) / (float) (SEASONED_AGE_TICKS - FRESH_AGE_TICKS), 0.0F, 1.0F);
        return FRESH_PICK_CHANCE + (1.0F - FRESH_PICK_CHANCE) * seasoning;
    }

    @Nullable
    private static Player findVictimPlayer(Goose goose) {
        Player best = null;
        double bestScore = -Double.MAX_VALUE;
        for (Player player : goose.level().getEntitiesOfClass(Player.class, goose.getBoundingBox().inflate(HAND_SEARCH_RADIUS))) {
            if (player.isSpectator() || player.isCreative()) continue;
            if (!isDesirable(player.getMainHandItem()) && !isDesirable(player.getOffhandItem())) continue;
            if (Math.abs(player.getY() - goose.getY()) > REACHABLE_HEIGHT) continue;
            boolean grudged = goose.getGrudges().holdsGrudgeAgainst(player.getUUID());
            boolean pumpkin = player.getMainHandItem().is(Items.PUMPKIN_SEEDS) || player.getOffhandItem().is(Items.PUMPKIN_SEEDS);
            double score = (grudged ? 2.0E6 : 0.0) + (pumpkin ? 1.0E6 : 0.0) - goose.distanceToSqr(player);
            if (score > bestScore) {
                bestScore = score;
                best = player;
            }
        }
        return best;
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
