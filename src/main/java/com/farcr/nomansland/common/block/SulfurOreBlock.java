package com.farcr.nomansland.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class SulfurOreBlock extends Block {
    private static final int MAX_CHAIN = 220;
    private static final int MAX_EXPLOSIONS = 6;

    public SulfurOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        if (level instanceof ServerLevel serverLevel) scheduleFireCheck(serverLevel, pos);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (level instanceof ServerLevel serverLevel) scheduleFireCheck(serverLevel, pos);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!hasAdjacentFire(level, pos)) return;

        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            if (ToxicGasBlock.canFlowInto(level.getBlockState(target)) && random.nextFloat() < 0.5F) {
                ToxicGasBlock.place(level, target, ToxicGasBlock.MAX_DISPERSION);
                break;
            }
        }
        level.scheduleTick(pos, this, 20 + random.nextInt(20));
    }

    private void scheduleFireCheck(ServerLevel level, BlockPos pos) {
        if (hasAdjacentFire(level, pos) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 20);
        }
    }

    private static boolean hasAdjacentFire(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(BlockTags.FIRE)) return true;
        }
        return false;
    }

    public static boolean isSulfurOre(BlockState state) {
        return state.getBlock() instanceof SulfurOreBlock;
    }

    public static void chainExplode(ServerLevel level, BlockPos origin) {
        chainExplode(level, origin, new int[]{MAX_CHAIN, MAX_EXPLOSIONS});
    }

    private static void chainExplode(ServerLevel level, BlockPos pos, int[] budget) {
        if (budget[0] <= 0 || !isSulfurOre(level.getBlockState(pos))) return;
        budget[0]--;

        RandomSource random = level.random;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighbor);
            if (!neighborState.isAir() && !isSulfurOre(neighborState)
                    && neighborState.getFluidState().isEmpty()
                    && neighborState.getDestroySpeed(level, neighbor) >= 0.0F
                    && random.nextFloat() < 0.35F) {
                level.setBlock(neighbor, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        if (random.nextFloat() < 0.6F && ToxicGasBlock.canFlowInto(level.getBlockState(pos))) {
            ToxicGasBlock.place(level, pos, ToxicGasBlock.MAX_DISPERSION);
        } else if (random.nextFloat() < 0.06F && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
        }

        if (budget[1] > 0 && random.nextFloat() < 0.2F) {
            budget[1]--;
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.0F, Level.ExplosionInteraction.NONE);
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    chainExplode(level, pos.offset(dx, dy, dz), budget);
                }
            }
        }
    }
}
