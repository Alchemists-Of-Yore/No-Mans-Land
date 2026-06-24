package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MinersGillBlock extends BushBlock {
    public static final MapCodec<MinersGillBlock> CODEC = simpleCodec(MinersGillBlock::new);
    public static final int MAX_AGE = 3;
    public static final int MATURE_AGE = 1;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape SHAPE = box(3.0, 0.0, 3.0, 13.0, 11.0, 13.0);
    private static final int ABSORB_RANGE = 4;
    private static final int ABSORB_DISPERSION = 12;

    public MinersGillBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public @NotNull MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.MUSHROOM_GROW_BLOCK) || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return true;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        int age = state.getValue(AGE);
        if (age >= MAX_AGE) return;

        int absorbed = absorbGas(level, pos, ABSORB_RANGE, ABSORB_DISPERSION);
        if (absorbed <= 0) return;

        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.0);

        if (random.nextInt(3) == 0) level.setBlock(pos, state.setValue(AGE, age + 1), 2);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (level instanceof ServerLevel serverLevel && state.getValue(AGE) >= MATURE_AGE && !player.getMainHandItem().is(Items.SHEARS)) {
            sporeBurst(serverLevel, pos, state);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void sporeBurst(ServerLevel level, BlockPos pos, BlockState state) {
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 40, 0.6, 0.4, 0.6, 0.0);

        int count = 1 + level.random.nextInt(2) + (state.getValue(AGE) >= MAX_AGE ? 1 : 0);
        BlockState young = this.defaultBlockState();
        int placed = 0;
        for (int attempt = 0; attempt < 12 && placed < count; attempt++) {
            BlockPos target = pos.offset(level.random.nextInt(5) - 2, level.random.nextInt(3) - 1, level.random.nextInt(5) - 2);
            if (level.getBlockState(target).isAir() && young.canSurvive(level, target)) {
                level.setBlock(target, young, 3);
                placed++;
            }
        }
    }

    public static int absorbGas(ServerLevel level, BlockPos pos, int range, int maxDispersion) {
        int absorbed = 0;
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-range, -range, -range), pos.offset(range, range, range))) {
            if (absorbed >= maxDispersion) break;
            BlockState state = level.getBlockState(candidate);
            if (ToxicGasBlock.isToxicGas(state)) {
                absorbed += state.getValue(ToxicGasBlock.DISPERSION) + 1;
                level.removeBlock(candidate, false);
            }
        }
        return absorbed;
    }
}
