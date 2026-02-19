package com.farcr.nomansland.common.block.pots;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public record PotData(BlockState state, ResourceLocation variant) {
    public static PotData read(CompoundTag tag, HolderLookup.Provider registries) {
        BlockState state = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("state"));
        ResourceLocation variant = ResourceLocation.parse(tag.getString("variant"));

        return new PotData(state, variant);
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("state", NbtUtils.writeBlockState(state));
        tag.putString("variant", variant.toString());

        return tag;
    }
}
