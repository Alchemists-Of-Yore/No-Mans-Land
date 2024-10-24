package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.blockentity.RemainsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RemainsBlock extends BrushableBlock {

    public RemainsBlock(Block pTurnsInto, Properties pProperties, SoundEvent pBrushSound, SoundEvent pBrushCompletedSound) {
        super(pTurnsInto, pBrushSound, pBrushCompletedSound, pProperties);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof BrushableBlockEntity brushableblockentity) {
            brushableblockentity.checkReset();
        }

        if (FallingBlock.isFree(pLevel.getBlockState(pPos.below())) && pPos.getY() >= pLevel.getMinBuildHeight()) {
            FallingBlockEntity fallingblockentity = FallingBlockEntity.fall(pLevel, pPos, pState);
            fallingblockentity.disableDrop();
            pPos.below();
        }
    }

    @Override
    public void spawnAfterBreak(BlockState pState, ServerLevel pLevel, BlockPos pPos, ItemStack pStack, boolean pDropExperience) {
        int a = new Random().nextInt(10);
        if (a < 1) {
            //TODO: When Buried is readded/worked on, replace SKELETON with BURIED
            Skeleton buried = EntityType.SKELETON.create(pLevel);
            if (buried != null) {
                buried.moveTo((double) pPos.getX() + 0.5D, pPos.getY(), (double) pPos.getZ() + 0.5D, 0.0F, 0.0F);
                pLevel.addFreshEntity(buried);
                buried.spawnAnim();
            }
        }
    }

    @Override
    public void onBrokenAfterFall(Level pLevel, BlockPos pPos, FallingBlockEntity pFallingBlock) {
        Vec3 vec3 = pFallingBlock.getBoundingBox().getCenter();
        pLevel.levelEvent(2001, BlockPos.containing(vec3), Block.getId(pFallingBlock.getBlockState()));
        pLevel.gameEvent(pFallingBlock, GameEvent.BLOCK_DESTROY, vec3);
        int a = new Random().nextInt(10);
        if (a < 5) {
            //TODO: When Buried is readded/worked on, replace SKELETON with BURIED
            Skeleton buried = EntityType.SKELETON.create(pLevel);
            if (buried != null) {
                buried.moveTo((double) pPos.getX() + 0.5D, pPos.getY(), (double) pPos.getZ() + 0.5D, 0.0F, 0.0F);
                pLevel.addFreshEntity(buried);
                buried.spawnAnim();
            }
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RemainsBlockEntity(pPos, pState);
    }
}


