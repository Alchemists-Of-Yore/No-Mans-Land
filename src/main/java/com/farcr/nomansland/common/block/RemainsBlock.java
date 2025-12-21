package com.farcr.nomansland.common.block;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.blockentity.RemainsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RemainsBlock extends BrushableBlock {
    public RemainsBlock(Block turnsInto, SoundEvent brushSound, SoundEvent brushCompletedSound, Properties properties) {
        super(turnsInto, brushSound, brushCompletedSound, properties);
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        if (level.random.nextFloat() < NMLConfig.BURIED_SPAWNING_CHANCE.get()*4) {
            //TODO: When Buried is readded/worked on, replace SKELETON with BURIED
            Skeleton buried = EntityType.SKELETON.create(level);
            if (buried != null) {
                buried.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                level.addFreshEntity(buried);
                buried.spawnAnim();
            }
        }
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        if (level.random.nextFloat() < NMLConfig.BURIED_SPAWNING_CHANCE.get()*10) {
            //TODO: When Buried is readded/worked on, replace SKELETON with BURIED
            Skeleton buried = EntityType.SKELETON.create(level);
            if (buried != null) {
                buried.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                level.addFreshEntity(buried);
                buried.spawnAnim();
            }
        }

        super.onBrokenAfterFall(level, pos, fallingBlockEntity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RemainsBlockEntity(pos, state);
    }
}


