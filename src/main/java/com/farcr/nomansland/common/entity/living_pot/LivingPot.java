package com.farcr.nomansland.common.entity.living_pot;

import com.farcr.nomansland.common.block.pots.PotData;
import com.farcr.nomansland.common.block.pots.PotSize;
import com.farcr.nomansland.common.block.pots.PotTrait;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.world.saved_data.RegeneratingPotsData;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class LivingPot extends PathfinderMob implements NeutralMob {

    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(LivingPot.class, EntityDataSerializers.INT);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(290, 310);
    public static final EntityDataAccessor<String> VARIANT = SynchedEntityData.defineId(LivingPot.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<BlockState> BLOCKSTATE = SynchedEntityData.defineId(LivingPot.class, EntityDataSerializers.BLOCK_STATE);
    public static final EntityDataAccessor<Boolean> DATA_IS_SLEEPING = SynchedEntityData.defineId(LivingPot.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DATA_LAST_ATTACK_TICK = SynchedEntityData.defineId(LivingPot.class, EntityDataSerializers.INT);

    @Nullable private UUID persistentAngerTarget;

    public BlockState blockState;
    public PotVariant variant;
    protected @Nullable ResourceKey<LootTable> lootTable;
    protected long lootTableSeed = 0L;
    public ItemStack storedItem = ItemStack.EMPTY;

    @Nullable private BlockPos homePos;
    private int returnTimer = 12000;

    private static final int MELEE_IMPACT_DELAY = 11;
    private static final int MELEE_COOLDOWN = 20;
    @Nullable private LivingEntity pendingMeleeTarget = null;
    private int pendingMeleeTickAt = -1;
    public int meleeCooldownTicks = 0;

    public boolean isDashing = false;
    public boolean hasWokenLargePot = false;
    private int wakeUpCooldownTicks = 0;
    private static final int WAKE_UP_COOLDOWN = 6000;
    private int wakeUpTicks = 0;

    public final AnimationState dashStartAnimState = new AnimationState();
    public final AnimationState dashLoopAnimState = new AnimationState();
    public final AnimationState dashEndAnimState = new AnimationState();
    public final AnimationState wakeUpAnimState = new AnimationState();
    public final AnimationState sleepAnimState = new AnimationState();
    public final AnimationState attackAnimState = new AnimationState();

    private int lastKnownAttackTick = -1;

    public LivingPot(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.blockState = NMLBlocks.ANCIENT_POT.get().defaultBlockState();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
        builder.define(VARIANT, "");
        builder.define(BLOCKSTATE, NMLBlocks.ANCIENT_POT.get().defaultBlockState());
        builder.define(DATA_IS_SLEEPING, false);
        builder.define(DATA_LAST_ATTACK_TICK, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.FOLLOW_RANGE, 20)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new LivingPotWakeUpLargeGoal(this));
        goalSelector.addGoal(1, new LivingPotDashGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false) {
            @Override public boolean canUse() { return isLarge() && super.canUse(); }
            @Override public boolean canContinueToUse() { return isLarge() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(2, new LivingPotFleeGoal(this, 1.2));
        goalSelector.addGoal(4, new LivingPotReturnHomeGoal(this));
        goalSelector.addGoal(5, new LivingPotWanderGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                living -> living instanceof Player p && p.canBeSeenAsEnemy() && !p.isInvisible() && isAngryAt(p)));
        targetSelector.addGoal(2, new HurtByTargetGoal(this) {
            @Override
            public void start() {
                super.start();
                if (getTarget() instanceof Player p) {
                    if (!p.canBeSeenAsEnemy() || p.isInvisible()) {
                        setTarget(null);
                    }
                }
            }
        });
        targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    private static final float LEG_HEIGHT = 5.0F / 16.0F;

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        if (variant != null) {
            AABB bounds = variant.shape().bounds();
            float width = (float) Math.max(bounds.getXsize(), bounds.getZsize());
            float height = (float) bounds.getYsize() + LEG_HEIGHT;
            return EntityDimensions.fixed(width, height);
        }
        if (isLarge()) return EntityDimensions.fixed(1.0F, 1.5625F + LEG_HEIGHT);
        return EntityDimensions.fixed(0.75F, 1.0F + LEG_HEIGHT);
    }

    public void setVariant(PotVariant variant, BlockState state) {
        this.variant = variant;
        this.blockState = state;
        entityData.set(VARIANT, level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant).toString());
        entityData.set(BLOCKSTATE, blockState);
        if (isSmall()) {
            Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(10);
            Objects.requireNonNull(getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.25);
            Objects.requireNonNull(getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(0.0);
        } else {
            Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(30);
            Objects.requireNonNull(getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.15);
            Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(6);
            Objects.requireNonNull(getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(1.0);
        }
        if (variant.traits().contains(PotTrait.BRITTLE)) {
            double halfHealth = Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).getBaseValue() / 2.0;
            Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(halfHealth);
        }
        setHealth(getMaxHealth());
        refreshDimensions();
    }

    @Override
    public boolean isPushable() {
        return isSmall();
    }

    @Override
    public boolean canBeCollidedWith() {
        return isLarge();
    }

    @Override
    protected void doPush(Entity other) {
        if (isSmall() && wakeUpTicks <= 0) {
            double dx = getX() - other.getX();
            double dz = getZ() - other.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.01) {
                Vec3 radial = new Vec3(dx / dist, 0, dz / dist);
                Vec3 pushVec = radial;
                if (other instanceof Player player) {
                    Vec3 motion = player.getDeltaMovement();
                    double horizontalSpeed = motion.horizontalDistance();
                    if (horizontalSpeed > 0.01) {
                        Vec3 forward = new Vec3(motion.x / horizontalSpeed, 0, motion.z / horizontalSpeed);
                        Vec3 right = new Vec3(-forward.z, 0, forward.x);
                        double dot = right.dot(position().subtract(other.position()));
                        Vec3 lateral = dot >= 0 ? right : right.scale(-1);
                        pushVec = radial.scale(0.6).add(lateral.scale(0.4));
                    }
                }
                double strength = 0.1;
                push(pushVec.x * strength, 0, pushVec.z * strength);
                return;
            }
        }
        super.doPush(other);
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        if (!level().isClientSide && isLarge() && isAngryAt(player) && player.isInvisible() && pendingMeleeTarget == null) {
            doHurtTarget(player);
        }
    }

    @Override
    public boolean fireImmune() {
        return variant == null || !variant.traits().contains(PotTrait.FLAMMABLE);
    }

    @Override
    protected int getBaseExperienceReward() {
        return random.nextInt(2, 6);
    }

    @Override
    public boolean shouldDropExperience() {
        return variant != null && variant.traits().contains(PotTrait.DROPS_EXPERIENCE);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            amount *= 3.0F;
        }
        if (isLarge() && isDashing) {
            amount *= 1.5F;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide) {
            Entity attacker = source.getEntity();
            if (attacker instanceof Player player && player.canBeSeenAsEnemy()) {
                startPersistentAngerTimer();
                setPersistentAngerTarget(player.getUUID());
                if (isSmall()) {
                    if (hasWokenLargePot) {
                        hasWokenLargePot = false;
                        wakeUpCooldownTicks = 0;
                    }
                    for (LivingPot other : level().getEntitiesOfClass(LivingPot.class, getBoundingBox().inflate(20),
                            p -> p != this && p.isLarge() && p.isAlive() && !p.isAngryAt(player))) {
                        other.startPersistentAngerTimer();
                        other.setPersistentAngerTarget(player.getUUID());
                        if (!player.isInvisible()) {
                            other.setTarget(player);
                        }
                    }
                }
            }
            if (variant != null && variant.traits().contains(PotTrait.INFESTED) && level() instanceof ServerLevel serverLevel) {
                int target = random.nextInt(2, 4);
                for (int i = 0; i < target; i++) {
                    if (random.nextFloat() < Math.min(amount, getMaxHealth()) / getMaxHealth()) {
                        Silverfish silverfish = EntityType.SILVERFISH.create(serverLevel);
                        if (silverfish != null) {
                            silverfish.moveTo(getX() + (random.nextDouble() - 0.5) * 0.5, getY(), getZ() + (random.nextDouble() - 0.5) * 0.5, random.nextFloat() * 360, 0);
                            serverLevel.addFreshEntity(silverfish);
                            silverfish.spawnAnim();
                        }
                    }
                }
            }
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!level().isClientSide && target instanceof LivingEntity living && meleeCooldownTicks <= 0) {
            pendingMeleeTarget = living;
            pendingMeleeTickAt = tickCount + MELEE_IMPACT_DELAY;
            meleeCooldownTicks = MELEE_COOLDOWN;
            setLastAttackTick(tickCount);
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.DECORATED_POT_HIT, SoundSource.HOSTILE,
                    1.0F, 0.6F + random.nextFloat() * 0.2F);
        }
        return true;
    }

    public void setLastAttackTick(int tick) {
        entityData.set(DATA_LAST_ATTACK_TICK, tick);
    }

    private static final byte WAKE_UP_EVENT = 60;

    public void startWakeUp() {
        this.wakeUpTicks = 30;
        level().broadcastEntityEvent(this, WAKE_UP_EVENT);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == WAKE_UP_EVENT) {
            wakeUpAnimState.start(tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    public void setSleeping(boolean sleeping) {
        entityData.set(DATA_IS_SLEEPING, sleeping);
    }


    @Override
    public boolean isNoAi() {
        if (level().isClientSide) return super.isNoAi();
        return super.isNoAi() || wakeUpTicks > 0;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (wakeUpTicks > 0) {
                wakeUpTicks--;
            }
            if (meleeCooldownTicks > 0) {
                meleeCooldownTicks--;
            }
            if (pendingMeleeTarget != null && tickCount >= pendingMeleeTickAt) {
                if (pendingMeleeTarget.isAlive() && distanceToSqr(pendingMeleeTarget) <= 6.0) {
                    float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
                    pendingMeleeTarget.hurt(damageSources().mobAttack(this), damage);
                }
                pendingMeleeTarget = null;
                pendingMeleeTickAt = -1;
            }
        }
        if (level().isClientSide) {

            if (entityData.get(DATA_IS_SLEEPING)) {
                sleepAnimState.startIfStopped(tickCount);
            } else {
                sleepAnimState.stop();
            }

            int serverAttackTick = entityData.get(DATA_LAST_ATTACK_TICK);
            if (serverAttackTick != lastKnownAttackTick && serverAttackTick > 0) {
                attackAnimState.start(tickCount);
                lastKnownAttackTick = serverAttackTick;
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        updatePersistentAnger((ServerLevel) level(), false);

        if (homePos == null) {
            homePos = blockPosition();
        }

        if (isIdle() && returnTimer > 0) {
            returnTimer--;
        }
        if (isSmall() && hasWokenLargePot) {
            wakeUpCooldownTicks++;
            if (wakeUpCooldownTicks >= WAKE_UP_COOLDOWN) {
                hasWokenLargePot = false;
                wakeUpCooldownTicks = 0;
            }
        }
        super.customServerAiStep();
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!level().isClientSide) {
            BlockState shatterState = blockState != null ? blockState : NMLBlocks.ANCIENT_POT.get().defaultBlockState();
            int particleCount = isLarge() ? 28 : 14;
            double spread = isLarge() ? 0.4 : 0.25;
            ((ServerLevel) level()).sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, shatterState),
                    getX(), getY() + (isLarge() ? 0.8 : 0.5), getZ(),
                    particleCount, spread, 0.3, spread, 0.15
            );
            float pitch = isLarge() ? 0.5F + random.nextFloat() * 0.2F : 0.8F + random.nextFloat() * 0.4F;
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.DECORATED_POT_SHATTER, SoundSource.HOSTILE,
                    1.2F, pitch);

            if (variant != null && variant.traits().contains(PotTrait.REGENERATES)) {
                ServerLevel serverLevel = (ServerLevel) level();
                ResourceLocation variantKey = level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant);
                if (variantKey != null && blockState != null) {
                    int delay = random.nextInt(20, 40) * 20;
                    RegeneratingPotsData.getOrDefault(serverLevel).addPot(blockPosition(), new PotData(blockState, variantKey), delay);
                }
            }
        }
        super.die(damageSource);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.DECORATED_POT_HIT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.DECORATED_POT_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundType.WOOD.getStepSound(), 0.1F, 0.8F + random.nextFloat() * 0.1F);
    }

    public boolean isIdle() {
        return getTarget() == null && !isAngry();
    }

    public boolean isSmall() {
        return variant != null && variant.size() == PotSize.SMALL;
    }

    public boolean isLarge() {
        return variant != null && variant.size() == PotSize.LARGE;
    }

    public void setHomePos(BlockPos pos) {
        this.homePos = pos;
    }

    @Nullable
    public BlockPos getHomePos() {
        return homePos;
    }

    public int getReturnTimer() {
        return returnTimer;
    }

    public void setStoredItem(ItemStack item) {
        this.storedItem = item.copy();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        ItemStack stack = unpackLootTable(damageSource.getEntity() instanceof Player player ? player : null);
        if (!stack.isEmpty()) spawnAtLocation(stack);
        if (!storedItem.isEmpty()) {
            if (storedItem.getItem() instanceof LingeringPotionItem potion) {
                level.playSound(null, getX(), getY(), getZ(),
                        SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL,
                        0.5F, 0.6F / (random.nextFloat() * 0.4F + 0.8F));
                Projectile projectile = potion.asProjectile(
                        level, position(), storedItem, Direction.UP);
                projectile.shoot(getX(), getY(), getZ(), 0.5F, 1);
                level.addFreshEntity(projectile);
            } else {
                spawnAtLocation(storedItem);
            }
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        tryLoadLootTable(tag);
        readPersistentAngerSaveData(level(), tag);

        if (tag.contains("Variant") && tag.contains("BlockState")) {
            PotVariant v = level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY)
                    .getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, ResourceLocation.parse(tag.getString("Variant"))))
                    .orElse(null);
            if (v != null) setVariant(v, NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), tag.getCompound("BlockState")));
        }
        NbtUtils.readBlockPos(tag, "HomePos").ifPresent(pos -> homePos = pos);
        if (tag.contains("ReturnTimer")) {
            returnTimer = tag.getInt("ReturnTimer");
        }
        if (tag.contains("StoredItem")) {
            storedItem = ItemStack.parseOptional(level().registryAccess(), tag.getCompound("StoredItem"));
        }
        hasWokenLargePot = tag.getBoolean("HasWokenLargePot");
        if (tag.contains("WakeUpCooldown")) {
            wakeUpCooldownTicks = tag.getInt("WakeUpCooldown");
        }
        if (tag.contains("WakeUpTicks")) {
            wakeUpTicks = tag.getInt("WakeUpTicks");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        trySaveLootTable(tag);
        addPersistentAngerSaveData(tag);

        if (variant != null && blockState != null) {
            Optional.ofNullable(level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant))
                    .ifPresent(key -> tag.putString("Variant", key.toString()));
            tag.put("BlockState", NbtUtils.writeBlockState(this.blockState));
        }
        if (homePos != null) {
            tag.put("HomePos", NbtUtils.writeBlockPos(homePos));
        }
        tag.putInt("ReturnTimer", returnTimer);
        if (!storedItem.isEmpty()) {
            tag.put("StoredItem", storedItem.save(level().registryAccess()));
        }
        tag.putBoolean("HasWokenLargePot", hasWokenLargePot);
        tag.putInt("WakeUpCooldown", wakeUpCooldownTicks);
        tag.putInt("WakeUpTicks", wakeUpTicks);
    }

    public boolean tryLoadLootTable(CompoundTag tag) {
        if (tag.contains("LootTable", 8)) {
            this.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(tag.getString("LootTable"))));
            if (tag.contains("LootTableSeed", 4)) {
                this.setLootTableSeed(tag.getLong("LootTableSeed"));
            } else {
                this.setLootTableSeed(0L);
            }
            return true;
        }
        return false;
    }

    public boolean trySaveLootTable(CompoundTag tag) {
        ResourceKey<LootTable> resourcekey = this.getPotLootTable();
        if (resourcekey == null) return false;
        tag.putString("LootTable", resourcekey.location().toString());
        long i = this.getLootTableSeed();
        if (i != 0L) tag.putLong("LootTableSeed", i);
        return true;
    }

    public ItemStack unpackLootTable(@Nullable Player player) {
        ResourceKey<LootTable> resourcekey = this.getPotLootTable();
        if (resourcekey != null && level() != null && level().getServer() != null) {
            LootTable loottable = level().getServer().reloadableRegistries().getLootTable(resourcekey);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer) player, resourcekey);
            }
            this.setLootTable(null);
            LootParams.Builder builder = new LootParams.Builder((ServerLevel) level())
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPosition()));
            if (player != null) {
                builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }
            var items = loottable.getRandomItems(builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
            return items.isEmpty() ? ItemStack.EMPTY : items.getFirst();
        }
        return ItemStack.EMPTY;
    }

    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) { this.lootTable = lootTable; }
    public void setLootTableSeed(long seed) { this.lootTableSeed = seed; }
    @Nullable public ResourceKey<LootTable> getPotLootTable() { return lootTable; }
    public long getLootTableSeed() { return lootTableSeed; }

    @Override
    public int getRemainingPersistentAngerTime() { return entityData.get(DATA_REMAINING_ANGER_TIME); }

    @Override
    public void setRemainingPersistentAngerTime(int time) { entityData.set(DATA_REMAINING_ANGER_TIME, time); }

    @Nullable @Override
    public UUID getPersistentAngerTarget() { return persistentAngerTarget; }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) { persistentAngerTarget = target; }

    @Override
    public void startPersistentAngerTimer() { setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(random)); }
}
