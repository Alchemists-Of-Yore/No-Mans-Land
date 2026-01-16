package com.farcr.nomansland.common.block.moonlight;

import com.farcr.nomansland.common.registry.NMLBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MoonlightBasinBlockEntity extends BlockEntity {
    public MoonlightBasinBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.MOONLIGHT_BASIN.get(), pos, blockState);
    }
}
