package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.block.AncestralCarvingBlock;
import com.farcr.nomansland.common.block.CarvingFormation;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class AncestralCarvingFeature extends Feature<NoneFeatureConfiguration> {

    private static final Direction[] ALL_FACINGS = Direction.values();

    public AncestralCarvingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        if (!level.getBlockState(origin).isAir()) return false;

        Direction[] facings = shuffled(ALL_FACINGS, random);

        for (Direction facing : facings) {
            BlockPos targetStone = origin.relative(facing.getOpposite());
            if (!level.getBlockState(targetStone).is(BlockTags.BASE_STONE_OVERWORLD)) continue;

            // Horizontal facings only have one valid orientation; rotation only varies for UP/DOWN.
            int rotation = facing.getAxis() == Direction.Axis.Y ? random.nextInt(4) : 0;
            Direction right = AncestralCarvingBlock.getPlaneRight(facing, rotation);
            Direction down = AncestralCarvingBlock.getPlaneDown(facing, rotation);

            for (int size = 3; size >= 1; size--) {
                int[] anchorOrder = shuffledIndices(size * size, random);
                for (int idx : anchorOrder) {
                    int anchorCol = idx / size;
                    int anchorRow = idx % size;
                    BlockPos gridOrigin = targetStone.relative(right, -anchorCol).relative(down, -anchorRow);
                    if (fits(level, gridOrigin, facing, right, down, size)) {
                        placeFormation(level, gridOrigin, facing, rotation, right, down, size);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean fits(WorldGenLevel level, BlockPos gridOrigin, Direction facing, Direction right, Direction down, int size) {
        int total = size * size;
        int exposed = 0;
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                BlockPos p = gridOrigin.relative(right, col).relative(down, row);
                if (!level.getBlockState(p).is(BlockTags.BASE_STONE_OVERWORLD)) return false;
                if (level.getBlockState(p.relative(facing)).isAir()) exposed++;
            }
        }
        return exposed * 4 >= total * 3;
    }

    private static void placeFormation(WorldGenLevel level, BlockPos gridOrigin, Direction facing, int rotation, Direction right, Direction down, int size) {
        BlockState base = NMLBlocks.ANCESTRAL_CARVING.get().defaultBlockState()
                .setValue(AncestralCarvingBlock.FACING, facing)
                .setValue(AncestralCarvingBlock.ROTATION, rotation);

        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                BlockPos p = gridOrigin.relative(right, col).relative(down, row);
                BlockState withFormation = base.setValue(AncestralCarvingBlock.FORMATION, CarvingFormation.getForPosition(size, col, row));
                level.setBlock(p, withFormation, 2);
            }
        }
    }

    private static int[] shuffledIndices(int n, RandomSource random) {
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = indices[i];
            indices[i] = indices[j];
            indices[j] = tmp;
        }
        return indices;
    }

    private static Direction[] shuffled(Direction[] source, RandomSource random) {
        Direction[] result = source.clone();
        for (int i = result.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction tmp = result[i];
            result[i] = result[j];
            result[j] = tmp;
        }
        return result;
    }

}
