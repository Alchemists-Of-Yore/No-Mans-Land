package com.farcr.nomansland.common.world.watershed;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.mixinextensions.WatershedNoiseRouterHolder;
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
        WatershedNoiseRouter noiseRouter = ((WatershedNoiseRouterHolder)(Object) randomState.router()).nml$watershedNoiseRouter();
        // positioned at the center of the watershed cell
        DensityFunction.FunctionContext watershedNoiseContext = new DensityFunction.SinglePointContext(
                watershedX * Watershed.WATERSHED_SIZE + (Watershed.WATERSHED_SIZE / 2), 0,
                watershedZ * Watershed.WATERSHED_SIZE + (Watershed.WATERSHED_SIZE / 2)
        );

        int drainMargin = 80;
        int drainX = random.nextInt(drainMargin, Watershed.WATERSHED_SIZE - drainMargin) + watershedX * Watershed.WATERSHED_SIZE,
            drainZ = random.nextInt(drainMargin, Watershed.WATERSHED_SIZE - drainMargin) + watershedZ * Watershed.WATERSHED_SIZE;
        int drainHeight = (int) noiseRouter.drainHeight.compute(new DensityFunction.SinglePointContext(drainX, 0, drainZ));

        int riverCount = (int) Math.round(noiseRouter.riverCount.compute(watershedNoiseContext));
        List<River> riverList = new ArrayList<>(riverCount);
        for (int i = 0; i < riverCount; i++) {

            int sourceMargin = 40;
            int sourceX = random.nextInt(sourceMargin, Watershed.WATERSHED_SIZE - sourceMargin) + watershedX * Watershed.WATERSHED_SIZE,
                sourceZ = random.nextInt(sourceMargin, Watershed.WATERSHED_SIZE - sourceMargin) + watershedZ * Watershed.WATERSHED_SIZE;

            int maximumHeightDifference = (int) (Math.sqrt((sourceX - drainX) * (sourceX - drainX) + (sourceZ - drainZ) * (sourceZ - drainZ)) / 8);
            int sourceHeight = (int) Math.min(noiseRouter.sourceHeight.compute(new DensityFunction.SinglePointContext(sourceX, 0, sourceZ)), drainHeight + maximumHeightDifference);

            River river = River.generate(random, watershedX, watershedZ, sourceX, sourceZ, sourceHeight, drainX, drainZ, drainHeight, 4);
            riverList.add(river);
        }

        NoMansLand.LOGGER.info("new watershed generated w/ drain at {} {} {}", drainX, drainHeight, drainZ);
        return new Watershed(
                watershedX, watershedZ,
                riverList,
                drainX, drainZ, drainHeight
        );
    }

}
