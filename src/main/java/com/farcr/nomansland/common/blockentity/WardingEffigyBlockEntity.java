package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.world.saved_data.WardedSpacesData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import static com.farcr.nomansland.common.block.WardingEffigyBlock.getRange;

public class WardingEffigyBlockEntity extends BlockEntity {
    public WardingEffigyBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.WARDING_EFFIGY.get(), pos, state);
    }

    @Override
    public void setLevel(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            WardedSpacesData wardedSpacesData = serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                WardedSpacesData::new, WardedSpacesData::create), WardedSpacesData.NAME);

            wardedSpacesData.addEffigy(getBlockPos(), getRange(getBlockState()));
        }

        super.setLevel(level);
    }
}
