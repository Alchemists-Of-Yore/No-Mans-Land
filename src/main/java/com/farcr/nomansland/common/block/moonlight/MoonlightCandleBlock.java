package com.farcr.nomansland.common.block.moonlight;

import com.farcr.nomansland.common.definitions.BlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoonlightCandleBlock extends Block implements SimpleWaterloggedBlock {

    public static final BooleanProperty CANDLE_LIT = AbstractCandleBlock.LIT;
    public static final VoxelShape CANDLE_SHAPE = Block.box(6, 0, 6, 10, 9, 10);

    public MoonlightCandleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(CANDLE_LIT, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CANDLE_LIT);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CANDLE_SHAPE;
    }
}
