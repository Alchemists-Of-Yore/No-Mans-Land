package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.block.pots.PotShatterParticleOption;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FallingPotEntity extends FallingBlockEntity {

    private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(FallingPotEntity.class, EntityDataSerializers.STRING);
    private double startY;

    public FallingPotEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public static FallingPotEntity fall(Level level, BlockPos pos, BlockState state) {
        FallingPotEntity entity = new FallingPotEntity(NMLEntities.FALLING_POT.get(), level);
        entity.blockState = state;
        entity.blocksBuilding = true;
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        entity.setDeltaMovement(0, 0, 0);
        entity.xo = pos.getX() + 0.5;
        entity.yo = pos.getY();
        entity.zo = pos.getZ() + 0.5;
        entity.setStartPos(pos);
        entity.startY = pos.getY();
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(entity);
        return entity;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, "");
    }

    public void setVariantId(ResourceLocation variantId) {
        entityData.set(DATA_VARIANT, variantId.toString());
    }

    public PotVariant getVariant() {
        String id = entityData.get(DATA_VARIANT);
        if (id.isEmpty()) return null;
        return level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY)
                .getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, ResourceLocation.parse(id)))
                .orElse(null);
    }

    public ResourceLocation getVariantModel() {
        PotVariant v = getVariant();
        return v != null ? v.model() : null;
    }

    public double getFallDistance() {
        return startY - getY();
    }

    public ResourceLocation getVariantId() {
        String id = entityData.get(DATA_VARIANT);
        return id.isEmpty() ? null : ResourceLocation.parse(id);
    }

    public void spawnShatterParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            ResourceLocation model = getVariantModel();
            if (model != null) {
                serverLevel.sendParticles(
                        new PotShatterParticleOption(model),
                        getX(), getY() + 0.5, getZ(),
                        20, 0.4, 0.3, 0.4, 0.15);
            }
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.DECORATED_POT_SHATTER, SoundSource.BLOCKS,
                    1.2F, 0.8F + random.nextFloat() * 0.4F);
        }
    }

    @Override
    public ItemEntity spawnAtLocation(ItemStack stack, float offsetY) {
        if (stack.is(getBlockState().getBlock().asItem())) {
            spawnShatterParticles();
            dropContents();
            return null;
        }
        return super.spawnAtLocation(stack, offsetY);
    }

    private void dropContents() {
        if (blockData == null || level().isClientSide) return;
        if (blockData.contains("Item")) {
            ItemStack stored = ItemStack.parseOptional(level().registryAccess(), blockData.getCompound("Item"));
            if (!stored.isEmpty()) {
                super.spawnAtLocation(stored, 0);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("PotVariant", entityData.get(DATA_VARIANT));
        tag.putDouble("StartY", startY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PotVariant")) {
            entityData.set(DATA_VARIANT, tag.getString("PotVariant"));
        }
        if (tag.contains("StartY")) {
            startY = tag.getDouble("StartY");
        }
    }
}
