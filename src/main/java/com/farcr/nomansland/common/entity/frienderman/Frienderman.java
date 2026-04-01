package com.farcr.nomansland.common.entity.frienderman;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.VanityIntegration;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class Frienderman extends EnderMan {
    private static final ResourceKey<LootTable> GIFT_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, NoMansLand.location("gameplay/frienderman_gift"));

    private boolean isPerformingStare;
    private boolean inspectingMask;
    private boolean waitingToDespawnAfterExchange;
    private int inspectTimer;
    @Nullable
    private Player barteringPlayer;

    public Frienderman(EntityType<? extends EnderMan> entityType, Level level) {
        super(entityType, level);
        setCanPickUpLoot(true);
        ItemStack mask = new ItemStack(NMLItems.ANCIENT_BRONZE_MASK.get());
        if (Mods.VANITY.isLoaded()) {
            VanityIntegration.applyWarpWornDesign(mask);
        }
        setItemSlot(EquipmentSlot.HEAD, mask);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    public void startPersistentAngerTimer() {
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return 0;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return null;
    }

    @Override
    public boolean isCreepy() {
        return false;
    }

    @Override
    public boolean hasBeenStaredAt() {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return false;
    }

    private boolean isAcceptableMask(ItemStack stack) {
        if (!stack.is(NMLItems.ANCIENT_BRONZE_MASK)) return false;
        if (Mods.VANITY.isLoaded() && VanityIntegration.hasDesign(stack)) return false;
        return true;
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return isAcceptableMask(stack) && !inspectingMask && !waitingToDespawnAfterExchange && Mods.VANITY.isLoaded();
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (isAcceptableMask(stack) && !inspectingMask && !waitingToDespawnAfterExchange && Mods.VANITY.isLoaded()) {
            this.onItemPickup(itemEntity);
            this.take(itemEntity, 1);
            ItemStack mask = stack.split(1);
            if (stack.isEmpty()) {
                itemEntity.discard();
            }
            Player nearest = level().getNearestPlayer(this, 10.0);
            startInspecting(mask, nearest);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FriendermanInspectMaskGoal(this));
        this.goalSelector.addGoal(1, new FriendermanStareGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(10, new FriendermanLeaveBlockGoal(this));
        this.goalSelector.addGoal(11, new FriendermanTakeBlockGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (isPerformingStare || inspectingMask || waitingToDespawnAfterExchange) return;

        Player nearestPlayer = level().getNearestPlayer(this, 4.0);
        if (nearestPlayer != null && !isWearingAlchemistMask(nearestPlayer)) {
            despawnWithEffects();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide()) return false;

        if (source.getEntity() instanceof LivingEntity) {
            despawnWithEffects();
            return false;
        }

        setHealth(getMaxHealth());
        teleport();
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && Mods.VANITY.isLoaded() && !inspectingMask && !waitingToDespawnAfterExchange) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (isAcceptableMask(heldItem)) {
                ItemStack mask = heldItem.split(1);
                startInspecting(mask, player);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    private void startInspecting(ItemStack mask, @Nullable Player player) {
        inspectingMask = true;
        inspectTimer = 40;
        barteringPlayer = player;
        setItemInHand(InteractionHand.MAIN_HAND, mask);
        getNavigation().stop();
    }

    private void performExchange() {
        ItemStack newMask = getItemInHand(InteractionHand.MAIN_HAND);
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        ItemStack oldMask = getItemBySlot(EquipmentSlot.HEAD);
        if (barteringPlayer != null && !oldMask.isEmpty()) {
            throwItemToward(oldMask.copy(), barteringPlayer);
        } else if (!oldMask.isEmpty()) {
            spawnAtLocation(oldMask.copy());
        }

        setItemSlot(EquipmentSlot.HEAD, newMask);

        waitingToDespawnAfterExchange = true;
        inspectTimer = 40;
    }

    public void despawnWithEffects() {
        if (inspectingMask) {
            ItemStack held = getItemInHand(InteractionHand.MAIN_HAND);
            if (!held.isEmpty()) spawnAtLocation(held);
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            inspectingMask = false;
            barteringPlayer = null;
        }
        waitingToDespawnAfterExchange = false;
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, xo, yo, zo, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 64, 0.5, 1.0, 0.5, 0.5);
            level().gameEvent(GameEvent.TELEPORT, position(), GameEvent.Context.of(this));
        }
        discard();
    }

    public void giftItems(Player target) {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(GIFT_LOOT_TABLE);
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .create(LootContextParamSets.PIGLIN_BARTER);

        for (ItemStack item : lootTable.getRandomItems(params)) {
            throwItemToward(item, target);
        }

        BlockState carriedBlock = getCarriedBlock();
        if (carriedBlock != null) {
            throwItemToward(new ItemStack(carriedBlock.getBlock()), target);
            setCarriedBlock(null);
        }
    }

    private void throwItemToward(ItemStack stack, LivingEntity target) {
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        ItemEntity itemEntity = new ItemEntity(level(), getX(), getY() + 1.0, getZ(), stack);
        itemEntity.setDeltaMovement(
                dx / horizontalDist * 0.3 + getRandom().nextGaussian() * 0.01,
                0.2 + getRandom().nextGaussian() * 0.01,
                dz / horizontalDist * 0.3 + getRandom().nextGaussian() * 0.01
        );
        itemEntity.setDefaultPickUpDelay();
        level().addFreshEntity(itemEntity);
    }

    public boolean isPlayerLookingAtMe(Player player) {
        Vec3 viewVec = player.getViewVector(1.0F).normalize();
        Vec3 toMe = new Vec3(getX() - player.getX(), getEyeY() - player.getEyeY(), getZ() - player.getZ());
        double dist = toMe.length();
        toMe = toMe.normalize();
        double dot = viewVec.dot(toMe);
        return dot > 1.0 - 0.025 / dist && player.hasLineOfSight(this);
    }

    public static void registerFriendermanRelatedGoals(Mob otherMob) {
        if (otherMob instanceof EnderMan enderman && !(enderman instanceof Frienderman)) {
            enderman.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(enderman, Frienderman.class, true));
        }
    }

    public static boolean isWearingAlchemistMask(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(NMLItems.ANCIENT_BRONZE_MASK);
    }

    private static class FriendermanInspectMaskGoal extends Goal {
        private final Frienderman frienderman;

        FriendermanInspectMaskGoal(Frienderman frienderman) {
            this.frienderman = frienderman;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return frienderman.inspectingMask || frienderman.waitingToDespawnAfterExchange;
        }

        @Override
        public void start() {
            frienderman.getNavigation().stop();
        }

        @Override
        public void tick() {
            frienderman.getNavigation().stop();
            if (frienderman.barteringPlayer != null) {
                frienderman.getLookControl().setLookAt(
                        frienderman.barteringPlayer.getX(),
                        frienderman.barteringPlayer.getEyeY(),
                        frienderman.barteringPlayer.getZ()
                );
            }
            frienderman.inspectTimer--;
            if (frienderman.inspectTimer <= 0) {
                if (frienderman.waitingToDespawnAfterExchange) {
                    frienderman.despawnWithEffects();
                } else {
                    frienderman.performExchange();
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return frienderman.inspectingMask || frienderman.waitingToDespawnAfterExchange;
        }
    }

    private static class FriendermanStareGoal extends Goal {
        private final Frienderman frienderman;
        @Nullable
        private Player staringPlayer;
        private int lookTimer;

        FriendermanStareGoal(Frienderman frienderman) {
            this.frienderman = frienderman;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            for (Player player : frienderman.level().getEntitiesOfClass(Player.class,
                    frienderman.getBoundingBox().inflate(64.0))) {
                if (frienderman.isPlayerLookingAtMe(player)) {
                    staringPlayer = player;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            frienderman.getNavigation().stop();
            frienderman.isPerformingStare = true;

            Vec3 lookDir = staringPlayer.getViewVector(1.0F).normalize();
            double targetX = staringPlayer.getX() + lookDir.x;
            double targetZ = staringPlayer.getZ() + lookDir.z;
            double targetY = staringPlayer.getY();

            Vec3 oldPos = frienderman.position();
            frienderman.level().playSound(null, oldPos.x, oldPos.y, oldPos.z,
                    SoundEvents.ENDERMAN_TELEPORT, frienderman.getSoundSource(), 1.0F, 1.0F);
            frienderman.teleportTo(targetX, targetY, targetZ);
            frienderman.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);

            double dx = staringPlayer.getX() - frienderman.getX();
            double dz = staringPlayer.getZ() - frienderman.getZ();
            float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            frienderman.setYRot(yaw);
            frienderman.setYHeadRot(yaw);
            frienderman.yRotO = yaw;

            lookTimer = 60;
        }

        @Override
        public void tick() {
            frienderman.getLookControl().setLookAt(staringPlayer.getX(), staringPlayer.getEyeY(), staringPlayer.getZ());
            lookTimer--;
            if (lookTimer <= 0) {
                if (isWearingAlchemistMask(staringPlayer)) {
                    frienderman.giftItems(staringPlayer);
                }
                frienderman.despawnWithEffects();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return lookTimer > 0 && staringPlayer != null && staringPlayer.isAlive();
        }

        @Override
        public void stop() {
            frienderman.isPerformingStare = false;
            staringPlayer = null;
        }
    }

    private static class FriendermanTakeBlockGoal extends Goal {
        private final Frienderman frienderman;

        FriendermanTakeBlockGoal(Frienderman frienderman) {
            this.frienderman = frienderman;
        }

        @Override
        public boolean canUse() {
            if (frienderman.isPerformingStare || frienderman.inspectingMask || frienderman.waitingToDespawnAfterExchange) return false;
            if (frienderman.getCarriedBlock() != null) return false;
            if (!EventHooks.canEntityGrief(frienderman.level(), frienderman)) return false;
            return frienderman.getRandom().nextInt(reducedTickDelay(20)) == 0;
        }

        @Override
        public void tick() {
            RandomSource randomsource = frienderman.getRandom();
            Level level = frienderman.level();
            int i = Mth.floor(frienderman.getX() - 2.0 + randomsource.nextDouble() * 4.0);
            int j = Mth.floor(frienderman.getY() + randomsource.nextDouble() * 3.0);
            int k = Mth.floor(frienderman.getZ() - 2.0 + randomsource.nextDouble() * 4.0);
            BlockPos blockpos = new BlockPos(i, j, k);
            BlockState blockstate = level.getBlockState(blockpos);
            Vec3 vec3 = new Vec3(frienderman.getBlockX() + 0.5, j + 0.5, frienderman.getBlockZ() + 0.5);
            Vec3 vec31 = new Vec3(i + 0.5, j + 0.5, k + 0.5);
            BlockHitResult blockhitresult = level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, frienderman));
            boolean flag = blockhitresult.getBlockPos().equals(blockpos);
            if (blockstate.is(BlockTags.ENDERMAN_HOLDABLE) && flag) {
                level.removeBlock(blockpos, false);
                level.gameEvent(GameEvent.BLOCK_DESTROY, blockpos, GameEvent.Context.of(frienderman, blockstate));
                frienderman.setCarriedBlock(blockstate.getBlock().defaultBlockState());
            }
        }
    }

    private static class FriendermanLeaveBlockGoal extends Goal {
        private final Frienderman frienderman;

        FriendermanLeaveBlockGoal(Frienderman frienderman) {
            this.frienderman = frienderman;
        }

        @Override
        public boolean canUse() {
            if (frienderman.isPerformingStare || frienderman.inspectingMask || frienderman.waitingToDespawnAfterExchange) return false;
            if (frienderman.getCarriedBlock() == null) return false;
            if (!EventHooks.canEntityGrief(frienderman.level(), frienderman)) return false;
            return frienderman.getRandom().nextInt(reducedTickDelay(2000)) == 0;
        }

        @Override
        public void tick() {
            RandomSource randomsource = frienderman.getRandom();
            Level level = frienderman.level();
            int i = Mth.floor(frienderman.getX() - 1.0 + randomsource.nextDouble() * 2.0);
            int j = Mth.floor(frienderman.getY() + randomsource.nextDouble() * 2.0);
            int k = Mth.floor(frienderman.getZ() - 1.0 + randomsource.nextDouble() * 2.0);
            BlockPos blockpos = new BlockPos(i, j, k);
            BlockState blockstate = level.getBlockState(blockpos);
            BlockPos below = blockpos.below();
            BlockState belowState = level.getBlockState(below);
            BlockState carriedState = frienderman.getCarriedBlock();
            if (carriedState != null) {
                carriedState = Block.updateFromNeighbourShapes(carriedState, frienderman.level(), blockpos);
                if (canPlaceBlock(level, blockpos, carriedState, blockstate, belowState, below)
                        && !EventHooks.onBlockPlace(
                        frienderman,
                        BlockSnapshot.create(level.dimension(), level, below),
                        Direction.UP)) {
                    level.setBlock(blockpos, carriedState, 3);
                    level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(frienderman, carriedState));
                    frienderman.setCarriedBlock(null);
                }
            }
        }

        private boolean canPlaceBlock(Level level, BlockPos pos, BlockState carriedState, BlockState destState, BlockState belowState, BlockPos belowPos) {
            return destState.isAir()
                    && !belowState.isAir()
                    && !belowState.is(Blocks.BEDROCK)
                    && !belowState.is(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST)
                    && belowState.isCollisionShapeFullBlock(level, belowPos)
                    && carriedState.canSurvive(level, pos)
                    && level.getEntities(frienderman, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(pos))).isEmpty();
        }
    }
}
