package com.farcr.nomansland.common.world.orevein;

import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

import java.util.Set;
import java.util.stream.Collectors;

public class OreVeinSystem {
    final Set<Holder<OreVein>> oreVeinTypes;

    public OreVeinSystem(WorldGenLevel level) {
        Registry<OreVein> registry = level.registryAccess().registryOrThrow(NMLRegistries.ORE_VEIN_KEY);
        this.oreVeinTypes = registry.asLookup().listElements()
                            .collect(Collectors.toUnmodifiableSet());
    }

    public void buildVeins(NoiseChunk noiseChunk, ChunkAccess chunk, WorldGenerationContext context, RandomState random) {
        int x = chunk.getPos().getMinBlockX(),
            z = chunk.getPos().getMinBlockZ();
        for (Holder<OreVein> oreVeinHolder : oreVeinTypes) {
            OreVein oreVein = oreVeinHolder.value();
            int cellX = Math.floorDiv(x, oreVein.spacing()),
                cellZ = Math.floorDiv(z, oreVein.spacing());

            RandomSource veinRandom = random.getOrCreateRandomFactory(
                    oreVeinHolder.getKey().location()
            ).at(cellX, 0, cellZ);

            int minX = cellX * oreVein.spacing(),
                minZ = cellX * oreVein.spacing();
            int maxX = minX + (oreVein.spacing() - oreVein.separation()),
                maxZ = minZ + (oreVein.spacing() - oreVein.separation());

            int veinCenterX = veinRandom.nextInt(minX, maxX),
                veinCenterZ = veinRandom.nextInt(minZ, maxZ);
        }
    }
}
