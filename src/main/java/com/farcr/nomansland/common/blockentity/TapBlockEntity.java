package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import static com.farcr.nomansland.common.block.tap.TapBlock.*;
import static net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL;
import static net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL;
import static net.minecraft.world.level.block.StairBlock.WATERLOGGED;

public class TapBlockEntity extends BlockEntity {
    public int timeEmptying;

    public TapBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.TAP.get(), pos, state);
        this.timeEmptying = 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TapBlockEntity tap) {

        // Ensure there is a cauldron within 3 blocks under the tap
        BlockPos cauldronPos = getCauldronPos(level, pos);
        boolean cauldronFound = cauldronPos != null;
        cauldronPos = cauldronFound ? cauldronPos : pos.below();
        BlockState cauldronState = level.getBlockState(cauldronPos);
        BlockState stateBehind = getBlockStateBehind(level, pos, state);
        BlockPos posBehind = pos.relative(state.getValue(FACING).getOpposite());
        Block cauldronBlock = cauldronState.getBlock();

        if ((stateBehind.hasProperty(WATERLOGGED) && stateBehind.getValue(WATERLOGGED)) || stateBehind.is(Blocks.WATER_CAULDRON)) {
            spawnDrippingParticles(level, pos, state, ParticleTypes.FALLING_WATER);
            if (cauldronFound && !((AbstractCauldronBlock) cauldronBlock).isFull(cauldronState) && (cauldronBlock instanceof CauldronBlock || cauldronState.is(Blocks.WATER_CAULDRON))) {
                tap.timeEmptying++;
                if (tap.timeEmptying > NMLConfig.TICKS_TO_FILL_CAULDRON.get()) {
                    if (cauldronState.hasProperty(LEVEL))
                        level.setBlockAndUpdate(cauldronPos, cauldronState.setValue(LEVEL, cauldronState.getValue(LEVEL) + 1));
                    else level.setBlockAndUpdate(cauldronPos, Blocks.WATER_CAULDRON.defaultBlockState());

                    if (cauldronState.hasProperty(WATERLOGGED) && !cauldronState.getValue(WATERLOGGED))
                        level.setBlockAndUpdate(cauldronPos, cauldronState.setValue(WATERLOGGED, true));
                    tap.timeEmptying = 0;
                }
            }
        }

        if (!cauldronFound) return;

        if (stateBehind.hasProperty(HONEY_LEVEL) && stateBehind.getValue(HONEY_LEVEL) == 5 && !((AbstractCauldronBlock) cauldronBlock).isFull(cauldronState) && (cauldronBlock instanceof CauldronBlock || cauldronState.is(NMLBlocks.HONEY_CAULDRON.block()))) {
            tap.timeEmptying++;
            if (tap.timeEmptying < NMLConfig.TICKS_TO_FILL_CAULDRON.get()) spawnDrippingParticles(level, pos, state, ParticleTypes.FALLING_HONEY);
            else {
                tap.timeEmptying = 0;
                if (cauldronState.hasProperty(FourLayeredCauldronBlock.LEVEL))
                    level.setBlockAndUpdate(cauldronPos, cauldronState.setValue(FourLayeredCauldronBlock.LEVEL, cauldronState.getValue(FourLayeredCauldronBlock.LEVEL) + 1));
                else level.setBlockAndUpdate(cauldronPos, NMLBlocks.HONEY_CAULDRON.get().defaultBlockState());
                BlockPos beehivePos = pos.relative(state.getValue(FACING).getOpposite());
                level.setBlockAndUpdate(beehivePos, stateBehind.setValue(HONEY_LEVEL, 0));
                level.gameEvent(GameEvent.BLOCK_CHANGE, beehivePos, GameEvent.Context.of(stateBehind));
                level.gameEvent(GameEvent.BLOCK_CHANGE, cauldronPos, GameEvent.Context.of(cauldronState));
            }
        }

        if ((stateBehind.is(Blocks.LAVA_CAULDRON) || stateBehind.is(Blocks.WATER_CAULDRON)) && !((AbstractCauldronBlock) cauldronBlock).isFull(cauldronState) && (cauldronBlock instanceof CauldronBlock || cauldronState.is(stateBehind.getBlock()))) {
            tap.timeEmptying++;
            if (tap.timeEmptying < NMLConfig.TICKS_TO_FILL_CAULDRON.get()) {
                spawnDrippingParticles(level, pos, state, stateBehind.is(Blocks.LAVA_CAULDRON) ? ParticleTypes.FALLING_LAVA : ParticleTypes.FALLING_WATER);
            } else {
                tap.timeEmptying = 0;
                if (stateBehind.is(Blocks.LAVA_CAULDRON)) {
                    level.setBlockAndUpdate(posBehind, Blocks.CAULDRON.defaultBlockState());
                    level.setBlockAndUpdate(cauldronPos, Blocks.LAVA_CAULDRON.defaultBlockState());
                } else {
                    LayeredCauldronBlock.lowerFillLevel(stateBehind, level, posBehind);
                    if (cauldronState.hasProperty(LEVEL))
                        level.setBlockAndUpdate(cauldronPos, cauldronState.setValue(LEVEL, cauldronState.getValue(LEVEL) + 1));
                    else level.setBlockAndUpdate(cauldronPos, stateBehind.getBlock().defaultBlockState());
                }

                level.gameEvent(GameEvent.BLOCK_CHANGE, cauldronPos, GameEvent.Context.of(cauldronState));
                level.gameEvent(GameEvent.BLOCK_CHANGE, posBehind, GameEvent.Context.of(stateBehind));
            }
        }

        if (stateBehind.getBlock() instanceof FourLayeredCauldronBlock fourLayeredCauldronBlock && !((AbstractCauldronBlock) cauldronBlock).isFull(cauldronState)) {
            tap.timeEmptying++;
            if (tap.timeEmptying < NMLConfig.TICKS_TO_FILL_CAULDRON.get() && fourLayeredCauldronBlock.particleType != null)
                spawnDrippingParticles(level, pos, state, fourLayeredCauldronBlock.particleType);
            else {
                tap.timeEmptying = 0;
                FourLayeredCauldronBlock.lowerFillLevel(stateBehind, level, posBehind);
                if (cauldronState.hasProperty(FourLayeredCauldronBlock.LEVEL))
                    level.setBlockAndUpdate(cauldronPos, stateBehind.setValue(FourLayeredCauldronBlock.LEVEL, cauldronState.getValue(FourLayeredCauldronBlock.LEVEL) + 1));
                else level.setBlockAndUpdate(cauldronPos, stateBehind.getBlock().defaultBlockState());
                level.gameEvent(GameEvent.BLOCK_CHANGE, cauldronPos, GameEvent.Context.of(cauldronState));
                level.gameEvent(GameEvent.BLOCK_CHANGE, posBehind, GameEvent.Context.of(stateBehind));
            }
        }
    }
}
