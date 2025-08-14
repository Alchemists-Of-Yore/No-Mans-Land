package com.farcr.nomansland.common.world.densityfunction;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.watershed.River;
import com.farcr.nomansland.common.world.watershed.Watershed;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public record CaveRiverShoreDensityFunction(DensityFunction horizontalDistance, DensityFunction riverHeight, DensityFunction riverRadius, WatershedMap watershedMap) implements DensityFunction {
    public static final KeyDispatchDataCodec<CaveRiverShoreDensityFunction> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new CaveRiverShoreDensityFunction(DensityFunctions.constant(0), DensityFunctions.constant(0), DensityFunctions.constant(0), null))
    );

    private double shorelineDensity(River.RiverSpaceCoordinates river, int blockX, int blockY, int blockZ, double horizontalDistance, double riverHeight, double riverRadius) {
        double squishFactor = 12.0;
        double surfaceHeight = River.getWaterSurfaceHeight((int) riverHeight, 0.2) - 30 / squishFactor + 4;
        double verticalDistance = blockY - surfaceHeight;

//        double shorelineMultiplierVertical = Mth.clampedMap(verticalDistance, -2, 2, 0, 1);
//        shorelineMultiplierVertical = Mth.smoothstep(shorelineMultiplierVertical);
//        shorelineMultiplierVertical = Mth.lerp(shorelineMultiplierVertical, 2, 0.25);
//        double shorelineMultiplierHorizontal = Mth.clampedMap(verticalDistance, -20, 2, 0, 1);
//        shorelineMultiplierHorizontal = Mth.smoothstep(shorelineMultiplierHorizontal);
//        shorelineMultiplierHorizontal = Mth.lerp(shorelineMultiplierHorizontal, 1, 0.5);
//
//        double finalHorizontalDistance = horizontalDistance / shorelineMultiplierHorizontal;
//        double finalVerticalDistance = verticalDistance / shorelineMultiplierVertical;
        if (verticalDistance > 0) {
            verticalDistance *= squishFactor;
        } else {
            verticalDistance /= 1.5;
        }
        return Math.sqrt(horizontalDistance * horizontalDistance + verticalDistance * verticalDistance) - 30;
    }

    private double computeWithInfo(int x, int y, int z, double horizontalDistance, double riverHeight, double riverRadius) {
        Watershed watershed = this.watershedMap.watershedAtBlock(x, z);
        River.RiverSpaceCoordinates river = watershed.river().getRiverSpaceCoordinates(x,y,z);
        if (river.horizontalDistance() >= 1000) return -0.5;
        double density = -shorelineDensity(river, x,y,z, horizontalDistance, riverHeight, riverRadius);

        return Mth.clampedMap(density, -30, 30, -0.8, 0.8);
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
        return visitor.apply(new CaveRiverShoreDensityFunction(this.horizontalDistance().mapAll(visitor), this.riverHeight().mapAll(visitor), this.riverRadius().mapAll(visitor), this.watershedMap()));
    }

    @Override
    public double minValue() {
        return -0.3;
    }

    @Override
    public double maxValue() {
        return 0.3;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
