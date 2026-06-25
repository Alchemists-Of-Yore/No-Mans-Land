package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GooseCarryBehavior extends Behavior<Goose> {
    private enum Plan { EAT, SHARE, WIELD, HORN, STASH }

    private static final float FLEE_SPEED = 1.5F;
    private static final float APPROACH_SPEED = 1.25F;
    private static final double REACHED_SQR = 4.0;
    private static final double FLOCK_RADIUS = 12.0;
    private static final double VICTIM_RADIUS_SQR = 100.0;
    private static final int WATER_SCAN_RADIUS = 10;
    private static final int WIELD_DURATION = 80;
    private static final int PECK_COOLDOWN = 20;
    private static final int MAX_INTERACT_TICKS = 200;
    private static final float MAX_WEAPON_BONUS = 5.0F;
    private static final int BITES_TO_FINISH = 3;
    private static final int HORN_BLOWS = 3;
    private static final double STASH_SEARCH_SQR = 144.0;
    private static final double DEPOSIT_DISTANCE_SQR = 1.8;
    private static final double DEPOSIT_KEEP_SQR = 3.5;

    private Plan plan = Plan.STASH;
    private int fleeTicks;
    private int interactTicks;
    private boolean fleeing;
    @Nullable private BlockPos fleeTarget;
    @Nullable private LivingEntity victim;
    @Nullable private Goose shareTarget;
    @Nullable private BlockPos stashChest;
    private boolean chestOpen;
    private int depositDelay;
    private int wieldTicks;
    private int peckCooldown;
    private int eatDelay;
    private int bites;
    private int hornBlows;
    private int hornCooldown;

    public GooseCarryBehavior() {
        super(Map.of(), Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        return goose.isCarrying() && !inCombat(goose);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return goose.isCarrying() && !inCombat(goose);
    }

    private static boolean inCombat(Goose goose) {
        return goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                || goose.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET);
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        if (goose.getAggressionAnchor() == null) goose.setAnchor(goose.blockPosition());
        fleeing = true;
        fleeTicks = 50 + goose.getRandom().nextInt(50);
        interactTicks = MAX_INTERACT_TICKS;
        fleeTarget = findWater(goose);
        plan = choosePlan(goose);
        victim = null;
        shareTarget = null;
        wieldTicks = WIELD_DURATION;
        peckCooldown = 0;
        eatDelay = 8;
        bites = 0;
        hornBlows = 0;
        hornCooldown = 30;
        stashChest = null;
        chestOpen = false;
        depositDelay = 0;
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (goose.isFlying()) return;
        if (peckCooldown > 0) peckCooldown--;

        if (fleeing) {
            flee(goose);
            if (--fleeTicks <= 0) endFlee(goose);
            return;
        }

        if (--interactTicks <= 0) {
            goose.dropCarriedItem();
            return;
        }

        switch (plan) {
            case EAT -> consume(level, goose);
            case SHARE -> share(level, goose);
            case WIELD -> wield(level, goose);
            case HORN -> blowHorn(level, goose);
            case STASH -> stash(level, goose);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        if (chestOpen && stashChest != null) setContainerOpen(level, stashChest, false);
        goose.setRummaging(false);
        fleeTarget = null;
        victim = null;
        shareTarget = null;
        stashChest = null;
        chestOpen = false;
        goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private void flee(Goose goose) {
        if (fleeTarget != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, fleeTarget, FLEE_SPEED, 0);
            if (goose.blockPosition().distSqr(fleeTarget) <= REACHED_SQR) endFlee(goose);
            return;
        }
        if (goose.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) return;
        Player player = goose.level().getNearestPlayer(goose.getX(), goose.getY(), goose.getZ(), 16.0,
                entity -> entity instanceof Player other && !other.isCreative() && !other.isSpectator());
        if (player == null) return;
        Vec3 away = DefaultRandomPos.getPosAway(goose, 10, 4, player.position());
        if (away != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, BlockPos.containing(away), FLEE_SPEED, 0);
        }
    }

    private void endFlee(Goose goose) {
        if (!fleeing) return;
        fleeing = false;
        goose.honk();
    }

    private void consume(ServerLevel level, Goose goose) {
        ItemStack carried = goose.getCarriedItem();
        if (!isConsumable(goose, carried)) {
            stash(level, goose);
            return;
        }
        boolean drink = isDrink(carried);
        if (--eatDelay > 0) return;
        eatDelay = drink ? 8 : 12;
        goose.peck();
        goose.playSound(drink ? SoundEvents.GENERIC_DRINK : SoundEvents.GENERIC_EAT,
                0.5F, 1.1F + goose.getRandom().nextFloat() * 0.4F);
        if (++bites >= BITES_TO_FINISH) {
            applyConsumeEffects(goose, carried);
            ItemStack remainder = remainderOf(carried);
            carried.shrink(1);
            goose.setCarriedItem(!carried.isEmpty() ? carried : remainder);
            bites = 0;
        }
    }

    private static ItemStack remainderOf(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null && food.usingConvertsTo().isPresent()) {
            return food.usingConvertsTo().get().copy();
        }
        if (stack.is(Items.POTION) || stack.is(Items.HONEY_BOTTLE)) return new ItemStack(Items.GLASS_BOTTLE);
        if (stack.is(Items.MILK_BUCKET)) return new ItemStack(Items.BUCKET);
        return ItemStack.EMPTY;
    }

    private static void applyConsumeEffects(Goose goose, ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            goose.heal(Math.max(1.0F, food.nutrition() / 2.0F));
            for (FoodProperties.PossibleEffect possible : food.effects()) {
                if (goose.getRandom().nextFloat() < possible.probability()) {
                    goose.addEffect(new MobEffectInstance(possible.effect()));
                }
            }
        }
        PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        if (potion != null) {
            potion.forEachEffect(effect -> {
                if (effect.getEffect().value().isInstantenous()) {
                    effect.getEffect().value().applyInstantenousEffect(null, null, goose, effect.getAmplifier(), 1.0);
                } else {
                    goose.addEffect(effect);
                }
            });
        }
        if (stack.is(Items.MILK_BUCKET)) {
            goose.removeAllEffects();
        }
        if (food == null && potion == null) {
            goose.heal(2.0F);
        }
    }

    private void share(ServerLevel level, Goose goose) {
        if (shareTarget == null || !shareTarget.isAlive() || shareTarget.isCarrying()) {
            shareTarget = nearestFreeFlockmate(goose);
        }
        if (shareTarget == null) {
            stash(level, goose);
            return;
        }
        goose.getLookControl().setLookAt(shareTarget);
        if (goose.distanceToSqr(shareTarget) <= REACHED_SQR) {
            shareTarget.setCarriedItem(goose.getCarriedItem().copy());
            goose.setCarriedItem(ItemStack.EMPTY);
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(goose, shareTarget.blockPosition(), APPROACH_SPEED, 1);
        }
    }

    private void wield(ServerLevel level, Goose goose) {
        if (victim == null || !victim.isAlive()) {
            victim = randomVictim(goose);
        }
        if (victim == null || wieldTicks-- <= 0) {
            goose.dropCarriedItem();
            return;
        }
        BehaviorUtils.setWalkAndLookTargetMemories(goose, victim, APPROACH_SPEED, 0);
        if (peckCooldown == 0 && goose.isWithinMeleeAttackRange(victim)) {
            goose.peck();
            float damage = (float) goose.getAttributeValue(Attributes.ATTACK_DAMAGE)
                    + Mth.clamp(Goose.weaponBonus(goose.getCarriedItem()), 0, MAX_WEAPON_BONUS);
            victim.hurt(level.damageSources().mobAttack(goose), damage);
            peckCooldown = PECK_COOLDOWN;
        }
    }

    private void blowHorn(ServerLevel level, Goose goose) {
        Holder<Instrument> instrument = goose.getCarriedItem().get(DataComponents.INSTRUMENT);
        if (instrument == null) {
            stash(level, goose);
            return;
        }
        if (--hornCooldown > 0) return;
        hornCooldown = 60 + goose.getRandom().nextInt(40);
        level.playSound(null, goose.getX(), goose.getY(), goose.getZ(),
                instrument.value().soundEvent().value(), SoundSource.RECORDS, instrument.value().range() / 16.0F, 1.0F);
        goose.flapBriefly();
        if (++hornBlows >= HORN_BLOWS) plan = Plan.STASH;
    }

    private void stash(ServerLevel level, Goose goose) {
        ItemStack carried = goose.getCarriedItem();
        if (stashChest == null || !hasSpace(containerAt(level, stashChest), carried)) {
            goose.setRummaging(false);
            chestOpen = false;
            stashChest = findDepositContainer(level, goose, carried);
        }
        if (stashChest != null) {
            depositIntoChest(level, goose);
            return;
        }
        BlockPos spot = fleeTarget != null ? fleeTarget : goose.getAggressionAnchor();
        if (spot == null || goose.blockPosition().distSqr(spot) <= REACHED_SQR) {
            goose.dropCarriedItem();
            return;
        }
        BehaviorUtils.setWalkAndLookTargetMemories(goose, spot, APPROACH_SPEED, 1);
    }

    private void depositIntoChest(ServerLevel level, Goose goose) {
        Vec3 center = Vec3.atCenterOf(stashChest);
        double reach = goose.isRummaging() ? DEPOSIT_KEEP_SQR : DEPOSIT_DISTANCE_SQR;
        if (goose.distanceToSqr(center) > reach) {
            goose.setRummaging(false);
            goose.getLookControl().setLookAt(center.x, center.y, center.z);
            BehaviorUtils.setWalkAndLookTargetMemories(goose, stashChest, APPROACH_SPEED, 0);
            return;
        }
        goose.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        faceContainer(goose, center);
        goose.setRummaging(true);
        if (--depositDelay > 0) return;
        depositDelay = 14 + goose.getRandom().nextInt(10);

        Container container = containerAt(level, stashChest);
        if (container == null) {
            stashChest = null;
            goose.setRummaging(false);
            return;
        }
        if (!chestOpen) {
            setContainerOpen(level, stashChest, true);
            chestOpen = true;
            return;
        }
        ItemStack remaining = insert(container, goose.getCarriedItem());
        container.setChanged();
        goose.setCarriedItem(remaining);
        if (remaining.isEmpty() || !hasSpace(container, remaining)) {
            setContainerOpen(level, stashChest, false);
            chestOpen = false;
            goose.setRummaging(false);
            goose.honkCurious();
            stashChest = null;
        }
    }

    @Nullable
    private static Container containerAt(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof Container container ? container : null;
    }

    private static boolean hasSpace(@Nullable Container container, ItemStack stack) {
        if (container == null || stack.isEmpty()) return false;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack inSlot = container.getItem(slot);
            if (inSlot.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(inSlot, stack) && inSlot.getCount() < inSlot.getMaxStackSize()) return true;
        }
        return false;
    }

    private static ItemStack insert(Container container, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack inSlot = container.getItem(slot);
            if (!inSlot.isEmpty() && ItemStack.isSameItemSameComponents(inSlot, remaining)) {
                int space = inSlot.getMaxStackSize() - inSlot.getCount();
                if (space > 0) {
                    int move = Math.min(space, remaining.getCount());
                    inSlot.grow(move);
                    remaining.shrink(move);
                }
            }
        }
        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            if (container.getItem(slot).isEmpty()) {
                container.setItem(slot, remaining.copy());
                remaining = ItemStack.EMPTY;
            }
        }
        return remaining;
    }

    @Nullable
    private static BlockPos findDepositContainer(ServerLevel level, Goose goose, ItemStack stack) {
        BlockPos origin = goose.blockPosition();
        ChunkPos chunkPos = goose.chunkPosition();
        BlockPos closest = null;
        double best = Double.MAX_VALUE;
        for (int chunkX = chunkPos.x - 1; chunkX <= chunkPos.x + 1; chunkX++) {
            for (int chunkZ = chunkPos.z - 1; chunkZ <= chunkPos.z + 1; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) continue;
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    if (!(entry.getValue() instanceof Container container) || !hasSpace(container, stack)) continue;
                    BlockPos pos = entry.getKey();
                    double distance = origin.distSqr(pos);
                    if (distance < STASH_SEARCH_SQR && Math.abs(pos.getY() - origin.getY()) <= 4 && distance < best) {
                        best = distance;
                        closest = pos.immutable();
                    }
                }
            }
        }
        return closest;
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

    private Plan choosePlan(Goose goose) {
        ItemStack carried = goose.getCarriedItem();
        if (isConsumable(goose, carried)) return Plan.EAT;
        if (carried.get(DataComponents.INSTRUMENT) != null) return Plan.HORN;
        int roll = goose.getRandom().nextInt(100);
        if (Goose.weaponBonus(carried) > 0 && roll < 50) return Plan.WIELD;
        if (roll < 25 && nearestFreeFlockmate(goose) != null) return Plan.SHARE;
        return Plan.STASH;
    }

    private static boolean isEdible(Goose goose, ItemStack stack) {
        return stack.has(DataComponents.FOOD) || goose.isFood(stack);
    }

    private static boolean isConsumable(Goose goose, ItemStack stack) {
        return isEdible(goose, stack) || isDrink(stack);
    }

    private static boolean isDrink(ItemStack stack) {
        return stack.is(Items.POTION)
                || stack.is(Items.MILK_BUCKET)
                || stack.is(Items.HONEY_BOTTLE);
    }

    @Nullable
    private static Goose nearestFreeFlockmate(Goose goose) {
        Goose closest = null;
        double best = Double.MAX_VALUE;
        for (Goose other : goose.nearbyGeese(FLOCK_RADIUS)) {
            if (other.isBaby() || other.isCarrying()) continue;
            double distance = goose.distanceToSqr(other);
            if (distance < best) {
                best = distance;
                closest = other;
            }
        }
        return closest;
    }

    @Nullable
    private static LivingEntity randomVictim(Goose goose) {
        NearestVisibleLivingEntities visible = goose.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .orElse(NearestVisibleLivingEntities.empty());
        List<LivingEntity> candidates = new ArrayList<>();
        visible.findAll(entity -> entity != goose
                && !(entity instanceof Goose)
                && entity.attackable()
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()))
                && goose.distanceToSqr(entity) < VICTIM_RADIUS_SQR
        ).forEach(candidates::add);
        return candidates.isEmpty() ? null : candidates.get(goose.getRandom().nextInt(candidates.size()));
    }

    @Nullable
    private static BlockPos findWater(Goose goose) {
        BlockPos origin = goose.blockPosition();
        BlockPos closest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-WATER_SCAN_RADIUS, -2, -WATER_SCAN_RADIUS),
                origin.offset(WATER_SCAN_RADIUS, 2, WATER_SCAN_RADIUS))) {
            if (!goose.level().getFluidState(pos).is(FluidTags.WATER)) continue;
            double distance = origin.distSqr(pos);
            if (distance < best) {
                best = distance;
                closest = pos.immutable();
            }
        }
        return closest;
    }
}
