package com.farcr.nomansland.common.world.watershed;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.mixinextensions.NoiseRouterExtension;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WatershedMap {
    private final RandomState randomState;
    private final PositionalRandomFactory randomFactory;
    private final Map<Long, Watershed> map;
    private final Queue<Watershed> queue;
    private final int capacity;

    public WatershedMap(RandomState randomState, int capacity) {
        this.randomState = randomState;
        this.capacity = capacity;
        this.map = new ConcurrentHashMap<>(capacity);
        this.queue = new ConcurrentLinkedQueue<>();
        this.randomFactory = this.randomState.getOrCreateRandomFactory(NoMansLand.location("watershed"));
    }

    public Watershed watershedAtBlock(int blockX, int blockZ) {
        return this.getOrCreateWatershed(Math.floorDiv(blockX, Watershed.WATERSHED_SIZE), Math.floorDiv(blockZ, Watershed.WATERSHED_SIZE));
    }

    public Watershed getOrCreateWatershed(int watershedX, int watershedZ) {
        long key = ChunkPos.asLong(watershedX, watershedZ);

        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            Watershed watershed = createWatershed(watershedX, watershedZ);
            if (queue.size() > capacity) {
                Watershed removedWatershed = queue.poll();
                long removedWatershedKey = ChunkPos.asLong(removedWatershed.watershedX(), removedWatershed.watershedZ());
                map.remove(removedWatershedKey);
            }
            map.put(key, watershed);
            queue.add(watershed);
            return watershed;
        }
    }

    private Watershed createWatershed(int watershedX, int watershedZ) {
        RandomSource random = this.randomFactory.at(watershedX, 0, watershedZ);
        NoiseRouterExtension noiseRouter = (NoiseRouterExtension)(Object)(randomState.router());
        // positioned at the center of the watershed cell
        WatershedNoiseContext noiseContext = new WatershedNoiseContext(
                watershedX * Watershed.WATERSHED_SIZE + (Watershed.WATERSHED_SIZE / 2),
                watershedZ * Watershed.WATERSHED_SIZE + (Watershed.WATERSHED_SIZE / 2)
        );

        boolean hasRiver = random.nextDouble() < noiseRouter.nml$watershedProbabilityNoise().compute(noiseContext);
        if (!hasRiver) // don't bother computing the other variables if the watershed has no river
            return new Watershed(watershedX, watershedZ, hasRiver, River.NONE, 0, 0, 0, 0, 0, 0);

        int sourceMargin = 40;
        int sourceX = random.nextInt(sourceMargin, Watershed.WATERSHED_SIZE - sourceMargin) + watershedX * Watershed.WATERSHED_SIZE,
            sourceZ = random.nextInt(sourceMargin, Watershed.WATERSHED_SIZE - sourceMargin) + watershedZ * Watershed.WATERSHED_SIZE;
        int drainMargin = 80;
        int drainX = random.nextInt(drainMargin, Watershed.WATERSHED_SIZE - drainMargin) + watershedX * Watershed.WATERSHED_SIZE,
            drainZ = random.nextInt(drainMargin, Watershed.WATERSHED_SIZE - drainMargin) + watershedZ * Watershed.WATERSHED_SIZE;

        int sourceHeight = (int) noiseRouter.nml$watershedSourceHeightNoise().compute(noiseContext);
        int maximumFall = (int) (Math.sqrt((sourceX - drainX) * (sourceX - drainX) + (sourceZ - drainZ) * (sourceZ - drainZ)) / 8);

        int drainHeight = (int) Math.max(noiseRouter.nml$watershedDrainHeightNoise().compute(noiseContext), sourceHeight - maximumFall);

        River river = River.generate(random, watershedX, watershedZ, sourceX, sourceZ, sourceHeight, drainX, drainZ, drainHeight, 4);

        NoMansLand.LOGGER.info("new watershed generated w/ source at {} {} {} and drain at {} {} {}", sourceX, sourceHeight, sourceZ, drainX, drainHeight, drainZ);
        return new Watershed(
                watershedX, watershedZ,
                hasRiver, river,
                sourceX, sourceZ, sourceHeight,
                drainX, drainZ, drainHeight
        );
    }

    private record WatershedNoiseContext(int blockX, int blockZ) implements DensityFunction.FunctionContext {
        @Override
        public int blockY() {
            return 0;
        }
    }
}
