package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.block.AncestralCarvingBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
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

        int rotation = random.nextInt(4);
        int rolledSize = pickSize(random);
        Direction[] facings = shuffled(ALL_FACINGS, random);

        for (Direction facing : facings) {
            BlockPos targetStone = origin.relative(facing.getOpposite());
            if (!level.getBlockState(targetStone).is(BlockTags.BASE_STONE_OVERWORLD)) continue;

            Direction right = AncestralCarvingBlock.getPlaneRight(facing, rotation);
            Direction down = AncestralCarvingBlock.getPlaneDown(facing, rotation);

            for (int size = rolledSize; size >= 1; size--) {
                int[] anchorOrder = shuffledIndices(size * size, random);
                for (int idx : anchorOrder) {
                    int anchorCol = idx / size;
                    int anchorRow = idx % size;
                    BlockPos gridOrigin = targetStone.relative(right, -anchorCol).relative(down, -anchorRow);
                    if (fits(level, gridOrigin, facing, right, down, size)) {
                        placeFormation(level, gridOrigin, facing, rotation, right, down, size, random);
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

    private static void placeFormation(WorldGenLevel level, BlockPos gridOrigin, Direction facing, int rotation, Direction right, Direction down, int size, RandomSource random) {
        BlockState carving = NMLBlocks.ANCESTRAL_CARVING.get().defaultBlockState()
                .setValue(AncestralCarvingBlock.FACING, facing)
                .setValue(AncestralCarvingBlock.ROTATION, rotation);

        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                if (col == 0 && row == 0) continue;
                BlockPos p = gridOrigin.relative(right, col).relative(down, row);
                level.setBlock(p, carving, 2);
            }
        }
        level.setBlock(gridOrigin, carving, 2);

        if (size > 1) {
            int missing = pickMissing(random);
            if (missing > 0) {
                int[] indices = shuffledIndices(size * size, random);
                BlockState stone = Blocks.STONE.defaultBlockState();
                for (int i = 0; i < missing; i++) {
                    int col = indices[i] / size;
                    int row = indices[i] % size;
                    level.setBlock(gridOrigin.relative(right, col).relative(down, row), stone, 2);
                }
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

    private static int pickSize(RandomSource random) {
        int roll = random.nextInt(6);
        if (roll < 3) return 1;
        if (roll < 5) return 2;
        return 3;
    }

    private static int pickMissing(RandomSource random) {
        int roll = random.nextInt(10);
        if (roll < 7) return 0;
        if (roll < 9) return 1;
        return 2;
    }
}
