package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.entity.buddy.BuddyChunkAnchor;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class BuddyFairyRingFeature extends Feature<FoliageCircleFeatureConfiguration> {
    public BuddyFairyRingFeature(Codec<FoliageCircleFeatureConfiguration> codec) {
        super(codec);
    }

    private static final int MAX_ISLAND_CHUNKS = 4096;

    @Override
    public boolean place(FeaturePlaceContext<FoliageCircleFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        FoliageCircleFeatureConfiguration config = context.config();

        ServerLevel serverLevel = level.getLevel();
        if (!isIslandAnchorChunk(serverLevel, origin)) return false;

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

    private static boolean isIslandAnchorChunk(ServerLevel level, BlockPos origin) {
        BiomeSource biomeSource = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        int quartY = QuartPos.fromBlock(origin.getY());

        ChunkPos originChunk = new ChunkPos(origin);
        Holder<Biome> islandBiome = biomeAt(biomeSource, sampler, originChunk, quartY);

        if (sameBiome(biomeSource, sampler, new ChunkPos(originChunk.x, originChunk.z - 1), quartY, islandBiome)) return false;
        if (sameBiome(biomeSource, sampler, new ChunkPos(originChunk.x - 1, originChunk.z), quartY, islandBiome)) return false;

        Set<Long> visited = new HashSet<>();
        ArrayDeque<ChunkPos> queue = new ArrayDeque<>();
        visited.add(originChunk.toLong());
        queue.add(originChunk);

        ChunkPos anchorChunk = originChunk;
        while (!queue.isEmpty() && visited.size() <= MAX_ISLAND_CHUNKS) {
            ChunkPos current = queue.poll();
            if (current.z < anchorChunk.z || (current.z == anchorChunk.z && current.x < anchorChunk.x)) anchorChunk = current;

            for (ChunkPos neighbour : new ChunkPos[]{
                new ChunkPos(current.x + 1, current.z), new ChunkPos(current.x - 1, current.z),
                new ChunkPos(current.x, current.z + 1), new ChunkPos(current.x, current.z - 1)}) {
                if (!visited.add(neighbour.toLong())) continue;
                if (sameBiome(biomeSource, sampler, neighbour, quartY, islandBiome)) queue.add(neighbour);
            }
        }

        return anchorChunk.equals(originChunk);
    }

    private static Holder<Biome> biomeAt(BiomeSource biomeSource, Climate.Sampler sampler, ChunkPos chunkPos, int quartY) {
        return biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkPos.getMiddleBlockX()), quartY, QuartPos.fromBlock(chunkPos.getMiddleBlockZ()), sampler);
    }

    private static boolean sameBiome(BiomeSource biomeSource, Climate.Sampler sampler, ChunkPos chunkPos, int quartY, Holder<Biome> islandBiome) {
        return biomeAt(biomeSource, sampler, chunkPos, quartY).equals(islandBiome);
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
