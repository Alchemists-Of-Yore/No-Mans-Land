package com.farcr.nomansland.common.world.generation.fogsea;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.utility.FastNoiseLite;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class FogSeaGenerator {
    private static FastNoiseLite noise = new FastNoiseLite();
    static {
        noise.SetFractalType(FastNoiseLite.FractalType.None);
        noise.SetFrequency(1.0F);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
    }

    private static float getFogSeaNoise(int x, int z) {
        return noise.GetNoise(x / 1600.0F, z / 1600.0F) * 0.988F
                + noise.GetNoise(x / 24.0F, z / 24.0F) * 0.010F
                + noise.GetNoise(x / 9.0F, z / 9.0F) * 0.002F;
    }

    public static boolean isInFogSea(int x, int z) {
        //NoMansLand.LOGGER.info("{}, {}", x, z);
        return noise.GetNoise(x / 1600.0F, z / 1600.0F) < 0.07F;
    }

    public static void fillFogSeaNoise(ChunkAccess chunk, BlockState defaultBlockState) {
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);

        float[][] noiseSlice = new float[16][16];
        int chunkMinX = chunk.getPos().getMinBlockX(),
            chunkMinZ = chunk.getPos().getMinBlockZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                noiseSlice[x][z] = getFogSeaNoise(chunkMinX + x, chunkMinZ + z);
            }
        }

        // loop down through each section
        for (int sectionIndex = chunk.getSectionsCount() - 1; sectionIndex >= 0; sectionIndex--) {
            LevelChunkSection currentSection = chunk.getSection(sectionIndex);

            for (int y = 15; y >= 0; y--) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int worldHeight = oceanFloor.getHighestTaken(x, z);
                        double newWorldHeight = Mth.clampedMap(noiseSlice[x][z], -0.02, 0.02, 0, 1);
                        newWorldHeight = Mth.smoothstep(newWorldHeight);
                        newWorldHeight = Mth.map(newWorldHeight, 0, 1, chunk.getMinBuildHeight(), worldHeight);
                        if (y + (sectionIndex * 16) + chunk.getMinBuildHeight() > newWorldHeight &&
                                (newWorldHeight != worldHeight)) //&& currentSection.getBlockState(x, y, z) == defaultBlockState)
                            currentSection.setBlockState(x, y, z, Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
        }
    }
}
