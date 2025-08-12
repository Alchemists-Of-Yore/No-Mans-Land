package com.farcr.nomansland.common.world.densityfunction;

import com.farcr.nomansland.common.world.watershed.Watershed;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import com.farcr.nomansland.common.world.watershed.River;
import com.farcr.nomansland.utility.MathUtilities;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public record CaveRiverDistanceDensityFunction(DensityFunction riverRadius, WatershedMap watershedMap) implements DensityFunction {
    public static final KeyDispatchDataCodec<CaveRiverDistanceDensityFunction> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new CaveRiverDistanceDensityFunction(DensityFunctions.constant(20), null))
    );

    private double ceilingDensity(River.RiverSpaceCoordinates river, int blockX, int blockY, int blockZ, double riverRadius) {
        double horizontalDistance = river.horizontalDistance(),
                riverHeight = river.riverHeight();
        double verticalDistance = blockY - riverHeight;

        double ceilingMultiplier = Mth.clampedMap(verticalDistance, 0, 10, 0, 1);
        double sharpness = 2;
        ceilingMultiplier = (Math.pow(2, sharpness) * 0.5) * Math.pow(Math.abs(ceilingMultiplier - 0.5), sharpness) * Mth.sign(ceilingMultiplier - 0.5) + 0.5;
        ceilingMultiplier = Mth.lerp(ceilingMultiplier, 1, 2);
        ceilingMultiplier = Mth.lerp(Mth.smoothstep(Mth.clampedMap(verticalDistance, 0, 6, 0, 1)), 1, ceilingMultiplier);
        double finalHorizontalDistance = horizontalDistance; //* ceilingMultiplier;

        double finalVerticalDistance = verticalDistance;
//        if (finalVerticalDistance < 0) {
//            finalHorizontalDistance *= 0.1;
//        }

        return Math.sqrt(finalHorizontalDistance * finalHorizontalDistance + finalVerticalDistance * finalVerticalDistance) - riverRadius;
    }

    private double shorelineDensity(River.RiverSpaceCoordinates river, int blockX, int blockY, int blockZ, double riverRadius) {
        double horizontalDistance = river.horizontalDistance(),
                riverHeight = river.riverHeight();
        double surfaceHeight = River.getWaterSurfaceHeight((int) riverHeight, 0.2);
        double verticalDistance = blockY - surfaceHeight;

        double shorelineMultiplier = Mth.clampedMap(verticalDistance, -1, 3, 0, 1);
        shorelineMultiplier = Mth.lerp(Mth.smoothstep(shorelineMultiplier), 2, 1);

        double riverDepthMultiplier = Mth.clampedMap(verticalDistance, 2, -3, 1, 2);

        double finalHorizontalDistance = horizontalDistance * shorelineMultiplier;
        double finalVerticalDistance = verticalDistance * riverDepthMultiplier;

        if (finalVerticalDistance > 0) {
            finalVerticalDistance *= 0.1;
        }

        return Math.sqrt(finalHorizontalDistance * finalHorizontalDistance + finalVerticalDistance * finalVerticalDistance) - riverRadius;
    }

    private double computeWithInfo(int x, int y, int z, double riverRadius) {
        Watershed watershed = this.watershedMap.watershedAtBlock(x, z);
        River.RiverSpaceCoordinates river = watershed.river().getRiverSpaceCoordinates(x,y,z);
        if (river.horizontalDistance() >= 1000) return 1;

        double shorelineDensity = shorelineDensity(river, x,y,z, riverRadius);
        double ceilingDensity = ceilingDensity(river, x,y,z, riverRadius);

        double density = -MathUtilities.smoothMin(-shorelineDensity, -ceilingDensity, 2);
        return Mth.clampedMap(density, -20, 20, -0.5, 0.5);
    }

    @Override
    public double compute(FunctionContext context) {
        double riverRadius = riverRadius().compute(context);
        return computeWithInfo(context.blockX(), context.blockY(), context.blockZ(), riverRadius);
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        double[] riverRadiusArray = new double[array.length];
        this.riverRadius().fillArray(riverRadiusArray, contextProvider);
        for (int i = 0; i < array.length; i++) {
            FunctionContext context = contextProvider.forIndex(i);
            array[i] = computeWithInfo(context.blockX(), context.blockY(), context.blockZ(), riverRadiusArray[i]);
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new CaveRiverDistanceDensityFunction(this.riverRadius().mapAll(visitor), this.watershedMap()));
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
