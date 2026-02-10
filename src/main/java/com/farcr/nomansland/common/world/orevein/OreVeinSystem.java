package com.farcr.nomansland.common.world.orevein;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.world.InterpolatedNoiseField;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

public class OreVeinSystem {
    private static final ResourceLocation ORE_VEIN_RANDOM = NMLRegistries.ORE_VEIN_KEY.location();

    final Set<Holder<OreVein>> oreVeinTypes;
    final ThreadLocal<ObjectArrayList<OreVeinInstance>> instances;

    public OreVeinSystem(WorldGenLevel level) {
        Registry<OreVein> registry = level.registryAccess().registryOrThrow(NMLRegistries.ORE_VEIN_KEY);
        this.oreVeinTypes = registry.asLookup().listElements().collect(Collectors.toUnmodifiableSet());
        this.instances = ThreadLocal.withInitial(() -> new ObjectArrayList<>(this.oreVeinTypes.size()));
    }

    public void buildVeins(ChunkAccess chunk, WorldGenerationContext context, RandomState random, BlockState defaultBlock) {
        ObjectArrayList<OreVeinInstance> oreVeinsInChunk = this.collectVeinsInChunk(chunk, context, random);
        if (oreVeinsInChunk.isEmpty()) return;
        this.fill(oreVeinsInChunk, chunk, random, defaultBlock);
    }

    private ObjectArrayList<OreVeinInstance> collectVeinsInChunk(ChunkAccess chunk, WorldGenerationContext context, RandomState random) {
        ObjectArrayList<OreVeinInstance> oreVeinsInChunk = this.instances.get();
        oreVeinsInChunk.clear();
        int chunkMinX = chunk.getPos().getMinBlockX(),
            chunkMinZ = chunk.getPos().getMinBlockZ();
        for (Holder<OreVein> oreVeinHolder : oreVeinTypes) {
            OreVein oreVein = oreVeinHolder.value();
            int centerCellX = Math.floorDiv(chunkMinX, oreVein.spacing()),
                centerCellZ = Math.floorDiv(chunkMinZ, oreVein.spacing());

            // collect veins from all neighboring cells
            for (int cellXOffset = -1; cellXOffset <= 1; cellXOffset++) {
                for (int cellZOffset = -1; cellZOffset <= 1; cellZOffset++) {
                    int cellX = centerCellX + cellXOffset, cellZ = centerCellZ + cellZOffset;

                    RandomSource veinRandom =
                            random.getOrCreateRandomFactory(oreVeinHolder.getKey().location())
                                    .at(cellX, 0, cellZ);

                    if (veinRandom.nextFloat() > oreVein.probability()) continue;

                    int minY = oreVein.minHeight().sample(veinRandom, context),
                        maxY = oreVein.maxHeight().sample(veinRandom, context);
                    if (minY > maxY) continue;

                    int minX = cellX * oreVein.spacing(),
                        minZ = cellX * oreVein.spacing();
                    int maxX = minX + (oreVein.spacing() - oreVein.separation()),
                        maxZ = minZ + (oreVein.spacing() - oreVein.separation());
                    int centerX = veinRandom.nextInt(minX, maxX),
                        centerZ = veinRandom.nextInt(minZ, maxZ);

                    int radius = oreVein.radius().sample(veinRandom);

                    if (Mth.length(chunkMinX - centerX, chunkMinZ - centerZ) < radius + 24)
                        oreVeinsInChunk.add(new OreVeinInstance(oreVein, centerX, centerZ, minY, maxY, radius));
                }
            }
        }

        return oreVeinsInChunk;
    }

    private void fill(ObjectArrayList<OreVeinInstance> oreVeinsInChunk, ChunkAccess chunk, RandomState randomState, BlockState defaultBlock) {
        // find the maximum possible y for any ore vein
        int maxY = Integer.MIN_VALUE, minY = Integer.MAX_VALUE;
        for (OreVeinInstance oreVeinInstance : oreVeinsInChunk) {
            minY = Math.min(minY, oreVeinInstance.minY);
            maxY = Math.max(maxY, oreVeinInstance.maxY);
        }
        int maxHeight = Integer.MIN_VALUE;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                maxHeight = Math.max(chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z), maxHeight);
            }
        }
        //maxY = Math.max(maxY, maxHeight);

        ChunkPos chunkpos = chunk.getPos();
        int chunkMinX = chunkpos.getMinBlockX(),
            chunkMinZ = chunkpos.getMinBlockZ();
        int chunkHeight = chunk.getHeight(),
            chunkMinY = chunk.getMinBuildHeight();

        RandomSource fillRandom = randomState.getOrCreateRandomFactory(ORE_VEIN_RANDOM).at(chunkMinX, 0, chunkMinZ);

        NormalNoise oreVeinA = randomState.getOrCreateNoise(Noises.ORE_VEIN_A),
                    oreVeinB = randomState.getOrCreateNoise(Noises.ORE_VEIN_B),
                    oreGap = randomState.getOrCreateNoise(Noises.ORE_GAP);

        InterpolatedNoiseField oreVeinAField = new InterpolatedNoiseField(chunkHeight, 2, 2),
                               oreVeinBField = new InterpolatedNoiseField(chunkHeight, 2, 2),
                               oreGapField = new InterpolatedNoiseField(chunkHeight, 2, 2);

        int fieldStartY = minY - chunkMinY, fieldEndY = maxY - chunkMinY;
        oreVeinAField.fill(fieldStartY, fieldEndY, chunkMinX, chunkMinY, chunkMinZ, (x, y, z) -> oreVeinA.getValue(x * 4, y * 4, z * 4));
        oreVeinBField.fill(fieldStartY, fieldEndY, chunkMinX, chunkMinY, chunkMinZ, (x, y, z) -> oreVeinB.getValue(x * 4, y * 4, z * 4));
        oreGapField.fill(fieldStartY, fieldEndY, chunkMinX, chunkMinY, chunkMinZ, oreGap::getValue);

        for (int x = 0; x < 16; x++) {
            int worldX = x + chunkMinX;
            for (int z = 0; z < 16; z++) {
                int worldZ = z + chunkMinZ;

                LevelChunkSection chunkSection;

                int maxColumnY = Math.min(chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z), maxY);

                for (int y = maxColumnY; y >= minY; y--) {
                    int localY = y - chunkMinY;
                    int sectionY = y & 15;
                    int sectionIndex = chunk.getSectionIndex(y);
                    chunkSection = chunk.getSection(sectionIndex);

                    BlockState currentState = chunkSection.getBlockState(x, sectionY, z);
                    if (currentState != defaultBlock) continue;

                    double veinANoise = oreVeinAField.retrieve(x, localY, z),
                           veinBNoise = oreVeinBField.retrieve(x, localY, z),
                           veinGapNoise = oreGapField.retrieve(x, localY, z);
                    double veinRidgeNoise = Math.max(Math.abs(veinANoise), Math.abs(veinBNoise)) - 0.08;

                    for (OreVeinInstance vein : oreVeinsInChunk) {
                        BlockState veinState = getVeinState(worldX, y, worldZ, veinRidgeNoise, veinGapNoise, fillRandom, vein);
                        if (veinState != null) {
                            chunkSection.setBlockState(x, sectionY, z, veinState, false);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private BlockState getVeinState(int x, int y, int z, double veinRidgeNoise, double veinGapNoise, RandomSource random, OreVeinInstance vein) {

        int maxYDist = vein.maxY - y, minYDist = y - vein.minY;
        if (maxYDist < 0 || minYDist < 0) return null;
        int yDist = Math.min(maxYDist, minYDist);

        if (random.nextFloat() > 0.7F) return null;

        double xzDist = Mth.length(x - vein.x, z - vein.z);
        if (xzDist > vein.radius) return null;

        veinRidgeNoise += Mth.clampedMap(yDist, 0, 4, 0.08, 0);
        veinRidgeNoise += Mth.clampedMap(xzDist, vein.radius * 0.5, vein.radius, 0, 0.08);

        if (veinRidgeNoise >= 0.0) return null;

        if (veinGapNoise > -0.3F && random.nextFloat() < 0.2F) {
            return random.nextFloat() < 0.02F ?
                    vein.type.raw().resolve(y) :
                    vein.type.ore().resolve(y);
        } else {
            return vein.type.filler().resolve(y);
        }
    }

    private record OreVeinInstance(OreVein type, int x, int z, int minY, int maxY, int radius) {}
}
