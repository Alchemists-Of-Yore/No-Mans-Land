package com.farcr.nomansland.common.entity.billhook_bass;

import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BillhookBass extends AbstractFish implements NeutralMob {

    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(BillhookBass.class, EntityDataSerializers.INT);

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    @Nullable
    private UUID persistentAngerTarget;

    public BillhookBass(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
    }

    public static void registerBillhookBassRelatedGoals(Mob otherMob) {
        if (otherMob instanceof AbstractFish fish && !(fish instanceof BillhookBass)) {
            fish.goalSelector.addGoal(3, new AvoidEntityGoal<>(fish, BillhookBass.class, 5.0F, 1.4, 1.2));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        addPersistentAngerSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        readPersistentAngerSaveData(level(), compound);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> goal instanceof PanicGoal);
        goalSelector.removeAllGoals(goal -> goal instanceof AvoidEntityGoal<?>);
        goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.3, false));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractFish.class, 10, true, true, target -> target.getType() != NMLEntities.BILLHOOK_BASS.get() && distanceTo(target) < 2));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));

    }

    @Override
    protected void customServerAiStep() {
        if (!level().isClientSide) {
            updatePersistentAnger((ServerLevel) level(), false);
            if (getTarget() != null && distanceTo(getTarget()) > 5)
                stopBeingAngry();
        }
        super.customServerAiStep();
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        if (entity instanceof AbstractFish fish) {
            fish.skipDropExperience();
            entity.nml$skipDroppingDeathLoot();
        }
        if (entity.getType() == EntityType.PUFFERFISH) addEffect(new MobEffectInstance(MobEffects.POISON, 12000));
        return super.killedEntity(level, entity);
    }

    @Override
    public @NotNull SoundEvent getFlopSound() {
        return NMLSounds.BASS_FLOP.get();
    }

    @Override
    public SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.BASS_HURT.get();
    }

    @Override
    public SoundEvent getDeathSound() {
        return NMLSounds.BASS_DEATH.get();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return NMLItems.BILLHOOK_BASS_BUCKET.stack();
    }

    @Override
    public EntityType<?> getType() {
        return NMLEntities.BILLHOOK_BASS.get();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8)
                .add(Attributes.ATTACK_DAMAGE, 4);
    }

    public int getRemainingPersistentAngerTime() {
        return entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    public void setRemainingPersistentAngerTime(int time) {
        entityData.set(DATA_REMAINING_ANGER_TIME, time);
    }

    @Nullable
    public UUID getPersistentAngerTarget() {
        return persistentAngerTarget;
    }

    public void setPersistentAngerTarget(@Nullable UUID target) {
        persistentAngerTarget = target;
    }

    public void startPersistentAngerTimer() {
        setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(random));
    }
}
