package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BuriedEntity extends AbstractSkeleton {

    public BuriedEntity(EntityType<? extends BuriedEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static void spawnFromRemains(ServerLevel level, double x, double y, double z) {
        BuriedEntity buried = NMLEntities.BURIED.get().create(level);
        if (buried == null) return;
        buried.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        buried.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(x, y, z)), MobSpawnType.SPAWNER, null);
        level.addFreshEntity(buried);
        buried.spawnAnim();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        if (random.nextFloat() < 0.15F) {
            equipGold(random, EquipmentSlot.HEAD, Items.GOLDEN_HELMET, 0.7F);
            equipGold(random, EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE, 0.35F);
            equipGold(random, EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS, 0.4F);
            equipGold(random, EquipmentSlot.FEET, Items.GOLDEN_BOOTS, 0.4F);
        }
    }

    private void equipGold(RandomSource random, EquipmentSlot slot, Item item, float chance) {
        if (random.nextFloat() < chance) {
            this.setItemSlot(slot, new ItemStack(item));
            this.armorDropChances[slot.getIndex()] = 0.35F;
        }
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength * 1.75, x, z);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }
}
