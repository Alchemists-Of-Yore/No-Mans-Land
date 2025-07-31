package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class CragRockFeature extends Feature<NoneFeatureConfiguration> {
    private static final NormalNoise NOISE = NormalNoise.create(RandomSource.create(0), 0, 1);

    public CragRockFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();

        double radius = Math.clamp(random.nextGaussian() * 2 + 5, 2, 6);
        int range = Math.min(Mth.ceil(radius + 3), 8);
        int height = random.nextIntBetweenInclusive(6, 18);//(int) Math.clamp(random.nextGaussian() * 16 + 32, 3, 24);
        int depth = 16;

        boolean placedBlock = false;
        BlockPos.MutableBlockPos pos = origin.mutable();
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                int totalSurfaceDepth = random.nextIntBetweenInclusive(2, 5);
                boolean wasAboveAir = false;
                boolean isSurface = false;

                int worldHeight = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX() + x, origin.getZ() + z);
                int surfaceDepth = 0;
                for (int y = height - 1; y >= -depth; y--) {
                    pos.set(origin.getX() + x, worldHeight + y, origin.getZ() + z);

                    double noiseFac = NOISE.getValue(pos.getX() * 0.15, pos.getY() * 0.02, pos.getZ() * 0.15);

                    double radFac;
                    if (pos.getY() > origin.getY() + 1) {
                        double distanceY = pos.getY() - origin.getY();
                        double radiusMultiplier = 1;
                        radiusMultiplier *= Mth.clampedMap(distanceY, 0, height - 1, 1, 0.7);
                        radiusMultiplier *= Mth.clampedMap(distanceY, height - 2, height - 1, 1, 0.8);
                        double dist = Math.sqrt(origin.distToCenterSqr(pos.getX() + 0.5, origin.getY() + 0.5, pos.getZ() + 0.5));
                        radFac = radius * radiusMultiplier - dist;
                    } else {
                        double dist = Math.sqrt(origin.distToCenterSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                        radFac = (radius + 1) - dist;
                    }

                    if (radFac + noiseFac * 8 > 0) {
                        if ((y > height - 3 || pos.getY() < worldHeight + 3) && !wasAboveAir) isSurface = true;
                        if (isSurface) surfaceDepth++;

                        BlockState state;
                        if (surfaceDepth == 1) {
                            state = Blocks.GRASS_BLOCK.defaultBlockState();
                        } else if (surfaceDepth < totalSurfaceDepth && surfaceDepth > 0) {
                            state = Blocks.DIRT.defaultBlockState();
                        } else {
                            state = Blocks.STONE.defaultBlockState();
                        }
                        level.setBlock(pos, state, 2);
                        placedBlock = true;
                        wasAboveAir = true;
                    } else {
                        wasAboveAir = false;
                        surfaceDepth = 0;
                    }
                }
            }
        }

        return placedBlock;
    }
}
