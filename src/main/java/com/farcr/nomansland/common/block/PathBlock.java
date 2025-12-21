package com.farcr.nomansland.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;


public class PathBlock extends DirtPathBlock {
    public final Block mainBlock;
    public final boolean hasGravity;

    public PathBlock(Properties properties, Block mainBlock, boolean hasGravity) {
        super(properties);
        this.mainBlock = mainBlock;
        this.hasGravity = hasGravity;
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return !this.defaultBlockState().canSurvive(pContext.getLevel(), pContext.getClickedPos()) ? Block.pushEntitiesUp(this.defaultBlockState(), mainBlock.defaultBlockState(), pContext.getLevel(), pContext.getClickedPos()) : super.getStateForPlacement(pContext);
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (this.hasGravity && FallingBlock.isFree(pLevel.getBlockState(pPos.below()))) {
            pLevel.scheduleTick(pPos, this, 2);
        }
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pFacing == Direction.UP && !pState.canSurvive(pLevel, pCurrentPos)) {
            pLevel.scheduleTick(pCurrentPos, this, 1);
        }

        if (this.hasGravity && FallingBlock.isFree(pLevel.getBlockState(pCurrentPos.below()))) {
            pLevel.scheduleTick(pCurrentPos, this, 2);
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pSource) {
        turnToBlock(null, pState, pLevel, pPos);
    }

    public void turnToBlock(@Nullable Entity pEntity, BlockState pState, Level pLevel, BlockPos pPos) {
        BlockState blockstate = pushEntitiesUp(pState, mainBlock.defaultBlockState(), pLevel, pPos);
        pLevel.setBlockAndUpdate(pPos, blockstate);
        pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pEntity, blockstate));
    }

}
