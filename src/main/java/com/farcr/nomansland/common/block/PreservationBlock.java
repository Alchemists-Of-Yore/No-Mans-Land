package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.world.saved_data.PreservedStructureData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PreservationBlock extends Block {

    public PreservationBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected void onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState newState, final boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof final ServerLevel serverLevel) {
            PreservedStructureData.get(serverLevel).unregister(pos.asLong());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
