package com.farcr.nomansland.common.entity.cervidae.deer;

import com.farcr.nomansland.common.entity.cervidae.ShedAntlersGoal;
import com.farcr.nomansland.common.entity.cervidae.IAntlers;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Deer extends Animal implements IAntlers {

    //TODO:
    // Sammy here, I've ripped out some of the Hydration related code from here and generally cleaned the class up.
    // Naturally, the code can be brought back by just reversing the changes to this class through github.
    // When it is time to properly implement Deer Drinking Behavior, contact me and we will figure something out.

    private static final EntityDataAccessor<Boolean> DATA_HAS_ANTLERS = SynchedEntityData.defineId(Deer.class, EntityDataSerializers.BOOLEAN);

    public int antlerTimer;

    public Deer(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HAS_ANTLERS, false);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        readAntlerData(compound);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        saveAntlerData(compound);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.JUMP_STRENGTH, 0.5)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.STEP_HEIGHT, 1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new PanicGoal(this, 1.75));
        goalSelector.addGoal(2, new BreedGoal(this, 1.25));
        goalSelector.addGoal(3, new FollowParentGoal(this, 1.5));
        goalSelector.addGoal(4, new ShedAntlersGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Monster.class, 12, 1.5, 1.75));
        goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Player.class, 12, 1.5, 1.75, player -> !player.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)));
        goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Villager.class, 12, 1.5, 1.75));
        goalSelector.addGoal(5, new AvoidEntityGoal<>(this, LivingEntity.class, 12, 1.5, 1.75, livingEntity -> livingEntity instanceof  NeutralMob));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean hasAntlers() {
        return entityData.get(DATA_HAS_ANTLERS);
    }

    @Override
    public void setHasAntlers(boolean hasAntlers) {
        entityData.set(DATA_HAS_ANTLERS, hasAntlers);
    }

    @Override
    public int getAntlerTimer() {
        return antlerTimer;
    }

    @Override
    public void setAntlerTimer(int antlersAge) {
        this.antlerTimer = antlersAge;
    }
    @Override
    public void onShedAntlers() {
        playSound(NMLSounds.DEER_SHED_ANTLERS.get(), 0.6F, 1.0F);
    }

    @Override
    protected void customServerAiStep() {
        if (!isBaby()) {
            tickAntlerGrowth(this);
        }

        super.customServerAiStep();
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (!isBaby() && random.nextFloat() < 0.8) {
            addAntlersUponSpawning(random);
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @SuppressWarnings("DataFlowIssue")
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob otherParent) {
        Deer offspringDeer = (Deer) getType().create(level());
        Deer otherDeer = (Deer) otherParent;
        offspringDeer.removeAntlersUponBirth(random);
        return offspringDeer;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(NMLTags.DEER_FOOD);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NMLSounds.DEER_AMBIENT.get();
    }
    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.DEER_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NMLSounds.DEER_DEATH.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        playSound(NMLSounds.DEER_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }
}
