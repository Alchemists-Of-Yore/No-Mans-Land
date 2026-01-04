package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class PotBlockEntity extends BlockEntity {

    public PotVariant variant = null;

    public PotBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.POT.get(), pos, state);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        Optional.ofNullable(level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant)).ifPresent(key -> {
            tag.putString("Variant", key.toString());
        });
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("Variant")) {
            variant = registries.lookupOrThrow(NMLRegistries.POT_VARIANT_KEY).get(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, ResourceLocation.parse(tag.getString("Variant")))).orElseThrow().value();
        }
    }

    public ItemStack getPotAsItem() {
        ItemStack itemstack = getBlockState().getBlock().asItem().getDefaultInstance();
        itemstack.applyComponents(this.collectComponents());
        return itemstack;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        Optional.ofNullable(level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant)).ifPresent(key -> {
            components.set(NMLDataComponents.POT_VARIANT, key);
        });
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        Optional.ofNullable(componentInput.get(NMLDataComponents.POT_VARIANT)).ifPresent(key -> {
            variant = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, key)).orElse(null);
        });
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("Variant");
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
