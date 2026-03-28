package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MoonCarvingBlockEntity extends BlockEntity {
    public MoonCarvingBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.MOON_CARVING.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MoonCarvingBlockEntity blockEntity) {
        NoMansLand.LOGGER.info("weh");
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
