package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.entity.buddy.BuddyChunkAnchor;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class BuddyFairyRingFeature extends Feature<FoliageCircleFeatureConfiguration> {
    public BuddyFairyRingFeature(Codec<FoliageCircleFeatureConfiguration> codec) {
        super(codec);
    }

    private static final int MIN_CHUNK_DISTANCE = 16;

    @Override
    public boolean place(FeaturePlaceContext<FoliageCircleFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        FoliageCircleFeatureConfiguration config = context.config();

        ServerLevel serverLevel = level.getLevel();
        if (!BuddyChunkAnchor.tryClaimChunk(serverLevel.dimension(), origin, MIN_CHUNK_DISTANCE)) {
            return false;
        }

        int radius = config.radius().sample(random);
        int count = 0;

        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;
        count += drawCircle(origin.getX(), origin.getZ(), x, y, level, random, config);
        while (y >= x) {
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            x++;
            count += drawCircle(origin.getX(), origin.getZ(), x, y, level, random, config);
        }

        if (count <= 0) return false;

        int anchorY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin).getY();
        BlockPos anchorPos = new BlockPos(origin.getX(), anchorY, origin.getZ());
        BuddyChunkAnchor.queuePendingAnchor(serverLevel.dimension(), anchorPos);
        return true;
    }

    private int drawCircle(int xc, int zc, int x, int z, WorldGenLevel level, RandomSource random, FoliageCircleFeatureConfiguration config) {
        int count = 0;
        count += placeBlock(xc + x, zc + z, level, random, config);
        count += placeBlock(xc - x, zc + z, level, random, config);
        count += placeBlock(xc + x, zc - z, level, random, config);
        count += placeBlock(xc - x, zc - z, level, random, config);
        count += placeBlock(xc + z, zc + x, level, random, config);
        count += placeBlock(xc - z, zc + x, level, random, config);
        count += placeBlock(xc + z, zc - x, level, random, config);
        count += placeBlock(xc - z, zc - x, level, random, config);
        return count;
    }

    private int placeBlock(int x, int z, WorldGenLevel level, RandomSource random, FoliageCircleFeatureConfiguration config) {
        BlockPos column = new BlockPos(x, 0, z);
        int y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column).getY();
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = config.state().getState(random, pos);
        if (state.canSurvive(level, pos)) {
            level.setBlock(pos, state, 3);
            return 1;
        }
        return 0;
    }
}
