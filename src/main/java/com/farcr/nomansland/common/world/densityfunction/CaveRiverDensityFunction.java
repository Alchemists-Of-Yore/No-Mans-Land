package com.farcr.nomansland.common.world.densityfunction;

import com.farcr.nomansland.common.world.watershed.River;
import com.farcr.nomansland.common.world.watershed.Watershed;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public record CaveRiverDensityFunction(DensityFunction horizontalDistance, DensityFunction riverHeight, DensityFunction riverRadius, WatershedMap watershedMap) implements DensityFunction {
    public static final KeyDispatchDataCodec<CaveRiverDensityFunction> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new CaveRiverDensityFunction(DensityFunctions.constant(0), DensityFunctions.constant(0), DensityFunctions.constant(0), null))
    );

    private double ceilingDensity(River.RiverSpaceCoordinates river, int blockX, int blockY, int blockZ, double horizontalDistance, double riverHeight, double riverRadius) {
        double verticalDistance = blockY - riverHeight;

        double ceilingMultiplier = Mth.clampedMap(verticalDistance, 0, 10, 0, 1);
        double sharpness = 2;
        ceilingMultiplier = (Math.pow(2, sharpness) * 0.5) * Math.pow(Math.abs(ceilingMultiplier - 0.5), sharpness) * Mth.sign(ceilingMultiplier - 0.5) + 0.5;
        ceilingMultiplier = Mth.lerp(ceilingMultiplier, 1, 2);
        ceilingMultiplier = Mth.lerp(Mth.smoothstep(Mth.clampedMap(verticalDistance, 0, 6, 0, 1)), 1, ceilingMultiplier);
        double finalHorizontalDistance = horizontalDistance; //* ceilingMultiplier;

        double finalVerticalDistance = verticalDistance;

        return Math.sqrt(finalHorizontalDistance * finalHorizontalDistance + finalVerticalDistance * finalVerticalDistance) - riverRadius;
    }

    private double shorelineDensity(River.RiverSpaceCoordinates river, int blockX, int blockY, int blockZ, double horizontalDistance, double riverHeight, double riverRadius) {
        double surfaceHeight = River.getWaterSurfaceHeight((int) riverHeight, 0.1);
        double verticalDistance = blockY - surfaceHeight;

        double shorelineMultiplier = Mth.clampedMap(verticalDistance, -1, 3, 0, 1);
        shorelineMultiplier = Mth.lerp(Mth.smoothstep(shorelineMultiplier), 3, 1);

        double riverDepthMultiplier = Mth.clampedMap(verticalDistance, 2, -3, 1, 2);

        double finalHorizontalDistance = horizontalDistance * shorelineMultiplier;
        double finalVerticalDistance = verticalDistance * riverDepthMultiplier;

        if (finalVerticalDistance > 0) {
            finalVerticalDistance *= 0.1;
        }

        return Math.sqrt(finalHorizontalDistance * finalHorizontalDistance + finalVerticalDistance * finalVerticalDistance) - riverRadius;
    }

    private double computeWithInfo(int x, int y, int z, double horizontalDistance, double riverHeight, double riverRadius) {
        Watershed watershed = this.watershedMap.watershedAtBlock(x, z);
        River.RiverSpaceCoordinates river = watershed.nearestRiverCoordinates(x,y,z);
        if (river.horizontalDistance() >= 1000) return 1;

        double shorelineDensity = shorelineDensity(river, x,y,z, horizontalDistance, riverHeight, riverRadius);
        double ceilingDensity = ceilingDensity(river, x,y,z, horizontalDistance, riverHeight, riverRadius);

        double density = Math.max(shorelineDensity, ceilingDensity);
        return Mth.clampedMap(density, -10, 10, -1.2, 1.2);
    }

    @Override
    public double compute(FunctionContext context) {
        double horizontalDistance = horizontalDistance().compute(context);
        double riverHeight = riverHeight().compute(context);
        double riverRadius = riverRadius().compute(context);

        return computeWithInfo(context.blockX(), context.blockY(), context.blockZ(), horizontalDistance, riverHeight, riverRadius);
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        double[] horizontalDistanceArray = new double[array.length];
        this.horizontalDistance().fillArray(horizontalDistanceArray, contextProvider);
        double[] riverHeightArray = new double[array.length];
        this.riverHeight().fillArray(riverHeightArray, contextProvider);
        double[] riverRadiusArray = new double[array.length];
        this.riverRadius().fillArray(riverRadiusArray, contextProvider);

        for (int i = 0; i < array.length; i++) {
            FunctionContext context = contextProvider.forIndex(i);
            array[i] = computeWithInfo(context.blockX(), context.blockY(), context.blockZ(),
                    horizontalDistanceArray[i],
                    riverHeightArray[i],
                    riverRadiusArray[i]
            );
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new CaveRiverDensityFunction(this.horizontalDistance().mapAll(visitor), this.riverHeight().mapAll(visitor), this.riverRadius().mapAll(visitor), this.watershedMap()));
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
