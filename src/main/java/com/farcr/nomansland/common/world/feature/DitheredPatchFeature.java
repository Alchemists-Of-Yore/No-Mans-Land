package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.NoMansLand;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/* "dithered_patch" feature type.

 places a patch on the ground with dithered edges.
 useful for ground coverings in forests and the like.

 EXAMPLE SYNTAX:

 {
  "type": "nomansland:dithered_patch",
  "config": {
    // required - no default
    //    takes in any variety of block provider.
    //    the block type to create the patch out of.
    "block_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "minecraft:coarse_dirt"
      }
    },
    // required - no default
    //    takes in any variety of block predicate.
    //    determines what block should be replaced!
    "target": {
      "type": "minecraft:matching_blocks",
      "blocks": "minecraft:grass_block"
    },
    // defaults to 6
    //   takes in any integer number provider. valid range: 1 - 8
    /    determines the approximate radius of the patch.
    "radius": {
      "type": "uniform",
      "min_inclusive": 3,
      "max_inclusive": 8
    },
    // defaults to 1
    //   takes in any integer number provider. valid range: 1 - infinity
    /    determines how far the patch extends below ground, tapering out at its edges.
    "depth": {
      "type": "uniform",
      "min_inclusive": 1,
      "max_inclusive": 4
    },
    // defaults to 0.5
    //   takes in any floating point number provider.
    //   determines how circular the shape of the patch is.
    "radius_strength": 0.5,
    // defaults to 5.0
    //   takes in any floating point number provider.
    //   determines how much the shape of the patch is affected by random noise.
    "noise_strength": 5.0,
    // defaults to 3.0
    //   takes in any floating point number provider.
    //   determines how dithered the patch appears.
    "dither_strength": 3.0
  }
 } */
public class DitheredPatchFeature extends Feature<DitheredPatchFeatureConfiguration> {
    private static final float[] DITHER_MATRIX = {
             0 / 16.0F,  8 / 16.0F,  2 / 16.0F, 10 / 16.0F,
            12 / 16.0F,  4 / 16.0F, 14 / 16.0F,  6 / 16.0F,
             3 / 16.0F, 11 / 16.0F,  1 / 16.0F,  9 / 16.0F,
            15 / 16.0F,  7 / 16.0F, 13 / 16.0F,  5 / 16.0F
    };
    private static final NormalNoise NOISE = NormalNoise.create(RandomSource.create(0), 0, 1);

    public DitheredPatchFeature(Codec<DitheredPatchFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DitheredPatchFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        DitheredPatchFeatureConfiguration config = context.config();
        BlockPos.MutableBlockPos mutableBlockPos = origin.mutable();

        int radius = config.radius().sample(random);
        int depth = config.depth().sample(random);

        float radiusStrength = config.radiusStrength().sample(random);
        float noiseStrength = config.noiseStrength().sample(random);
        float ditherStrength = config.ditherStrength().sample(random);

        BlockPredicate targetPredicate = config.target();
        BlockStateProvider blockProvider = config.blockProvider();
        float maxValue = radius * radiusStrength + noiseStrength + (15/16.0F) * ditherStrength;

        boolean placedBlock = false;
        int range = Math.min(radius + 4, 8);
        for (int xOffset = -range; xOffset <= range; xOffset++) {
            for (int zOffset = -range; zOffset <= range; zOffset++) {
                int x = origin.getX() + xOffset;
                int z = origin.getZ() + zOffset;
                int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;

                double radiusFac = radius - Math.sqrt(origin.distToCenterSqr(x, y, z));
                double noiseFac = NOISE.getValue(x * 0.2, 0, z * 0.2);
                float ditherFac = DITHER_MATRIX[Math.floorMod(x, 4) * 4 + Math.floorMod(z, 4)] - 0.5F;

                double facUndithered = radiusFac * radiusStrength + noiseFac * noiseStrength;
                double fac = facUndithered + ditherFac * ditherStrength;
                if (fac > 0) {
                    // function that tapers around the edges of the feature
                    int depthAtPos = Math.max(1, Mth.ceil(depth * Math.clamp(facUndithered / (maxValue * 0.3), 0, 1)));
                    for (int yOffset = 0; yOffset < depthAtPos; yOffset++) {
                        mutableBlockPos.set(x, y - yOffset, z);
                        if (targetPredicate.test(level, mutableBlockPos)) {
                            level.setBlock(mutableBlockPos, blockProvider.getState(random, mutableBlockPos), 2);
                            placedBlock = true;
                        }
                    }
                }
            }
        }

        return placedBlock;
    }
}
