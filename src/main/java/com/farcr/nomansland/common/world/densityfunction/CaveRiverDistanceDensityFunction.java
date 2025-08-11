package com.farcr.nomansland.common.world.densityfunction;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.watershed.Watershed;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public record CaveRiverDistanceDensityFunction(DensityFunction riverXOffset, DensityFunction riverZOffset, WatershedMap watershedMap) implements DensityFunction {
    public static final KeyDispatchDataCodec<CaveRiverDistanceDensityFunction> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new CaveRiverDistanceDensityFunction(DensityFunctions.constant(0), DensityFunctions.constant(0), null))
    );

    private double riverDistance(FunctionContext context, Watershed watershed, int blockX, int blockY, int blockZ) {

        double gradient = (blockX - watershed.drainX()) * (watershed.sourceX() - watershed.drainX()) +
                          (blockZ - watershed.drainZ()) * (watershed.sourceZ() - watershed.drainZ());
        gradient /= (watershed.sourceX() - watershed.drainX()) * (watershed.sourceX() - watershed.drainX()) +
                    (watershed.sourceZ() - watershed.drainZ()) * (watershed.sourceZ() - watershed.drainZ());
        double height = Mth.clampedMap(gradient, 0, 1, watershed.drainHeight(), watershed.sourceHeight());

        double riverDistanceVertical = blockY - height;

        double offsetX = riverXOffset.compute(context) * 100,
               offsetZ = riverZOffset.compute(context) * 100;
        double offsetInfluence = 1;
        if (gradient < 0.25) {
            offsetInfluence = gradient * 4;
        } else if (gradient > 0.75) {
            offsetInfluence = (1 - gradient) * 4;
        }
        double riverDistanceHorizontal = lineSegmentDistance(
                blockX + offsetX * offsetInfluence, blockZ + offsetZ * offsetInfluence,
                watershed.sourceX(), watershed.sourceZ(),
                watershed.drainX(), watershed.drainZ()
        );

        return Math.sqrt(riverDistanceHorizontal * riverDistanceHorizontal + riverDistanceVertical * riverDistanceVertical);
    }

    @Override
    public double compute(FunctionContext context) {
        Watershed watershed = this.watershedMap.watershedAtBlock(context.blockX(), context.blockZ());
        double distance = riverDistance(context, watershed, context.blockX(), context.blockY(), context.blockZ());
        return (distance - 10) < 0 ? -100 : 100;
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new CaveRiverDistanceDensityFunction(riverXOffset.mapAll(visitor), riverZOffset.mapAll(visitor), this.watershedMap()));
    }

    private double lineSegmentDistance(double px, double py, double ax, double ay, double bx, double by) {
        double bax = bx - ax, bay = by - ay;
        double h = ((px - ax) * bax + (py - ay) * bay) / (bax * bax + bay * bay);
        if (h < 0) h = 0;
        else if (h > 1) h = 1;
        double dx = (px - ax) - bax * h;
        double dy = (py - ay) - bay * h;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public double minValue() {
        return -10;
    }

    @Override
    public double maxValue() {
        return 60;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
