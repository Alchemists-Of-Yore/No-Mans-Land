package com.farcr.nomansland.common.world.densityfunction;

import com.farcr.nomansland.NoMansLand;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class CaveRiverTestDensityFunction implements DensityFunction.SimpleFunction {
    public static final KeyDispatchDataCodec<CaveRiverTestDensityFunction> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new CaveRiverTestDensityFunction())
    );

    private static NormalNoise noise = NormalNoise.create(RandomSource.create(0), 0, 1);

    private static int WATERSHED_SIZE = 512;
    private int watershedCoordinate(int blockCoordinate) {
        return Math.floorDiv(blockCoordinate, WATERSHED_SIZE);
    }
    private WatershedPosition watershedPosition(int blockX, int blockZ) {
        double watershedX = (double) Math.floorMod(blockX, WATERSHED_SIZE) / WATERSHED_SIZE;
        double watershedZ = (double) Math.floorMod(blockZ, WATERSHED_SIZE) / WATERSHED_SIZE;
        return new WatershedPosition(watershedX * 2 - 1, watershedZ * 2 - 1);
    }

    private WatershedPosition watershedGradient(int watershedCoordinateX, int watershedCoordinateZ) {
        double angle = noise.getValue(watershedCoordinateX, 0, watershedCoordinateZ) * 10000.0;
        return new WatershedPosition(Math.sin(angle) * 0.5, Math.cos(angle) * 0.5);
    }
    private WatershedPosition watershedSourcePos(int watershedCoordinateX, int watershedCoordinateZ) {
        WatershedPosition gradient = watershedGradient(watershedCoordinateX, watershedCoordinateZ);
        double angle = noise.getValue(watershedCoordinateX, 30, watershedCoordinateZ) * 10000.0;
        double amount = noise.getValue(watershedCoordinateX, 40, watershedCoordinateZ) * 0.25;

        return new WatershedPosition(gradient.x + Math.sin(angle) * amount, gradient.z + Math.cos(angle) * amount);
    }
    private double watershedSourceHeight(int watershedCoordinateX, int watershedCoordinateZ) {
        return -20;
    }

    private WatershedPosition watershedDrainPos(int watershedCoordinateX, int watershedCoordinateZ) {
        WatershedPosition gradient = watershedGradient(watershedCoordinateX, watershedCoordinateZ);
        double angle = noise.getValue(watershedCoordinateX, 10, watershedCoordinateZ) * 10000.0;
        double amount = noise.getValue(watershedCoordinateX, 20, watershedCoordinateZ) * 0.25;

        return new WatershedPosition(-gradient.x + Math.sin(angle) * amount, -gradient.z + Math.cos(angle) * amount);
    }
    private double watershedDrainHeight(int watershedCoordinateX, int watershedCoordinateZ) {
        return -50;
    }

    private double riverDistance(int blockX, int blockY, int blockZ) {
        int watershedX = watershedCoordinate(blockX),
            watershedZ = watershedCoordinate(blockZ);
        WatershedPosition posInWatershed = watershedPosition(blockX, blockZ);

        WatershedPosition watershedGradient = watershedGradient(watershedX, watershedZ);
        double sourceHeight = watershedDrainHeight(watershedX, watershedZ);
        double drainHeight = watershedSourceHeight(watershedX, watershedZ);

        double gradient = watershedGradient.x * (posInWatershed.x + watershedGradient.x) + watershedGradient.z * (posInWatershed.z + watershedGradient.z);
        gradient /= 2 * (watershedGradient.x * watershedGradient.x + watershedGradient.z * watershedGradient.z);
        double height = Mth.map(gradient, 0, 1, drainHeight, sourceHeight);

        double ceilingHeight = Mth.lerp(0.8, terrace(height, 10, 0.25), height) + 3;
        double waterLevel = terrace(height, 10, 0.05);

        double surfaceDistance = Math.min(Math.abs(blockY - ceilingHeight), Math.abs(blockY - waterLevel));
        surfaceDistance *= 1.5;
        if (blockY < ceilingHeight && blockY > waterLevel) surfaceDistance = 0;

        WatershedPosition sourcePos = watershedSourcePos(watershedX, watershedZ);
        WatershedPosition drainPos = watershedDrainPos(watershedX, watershedZ);

        double noiseX = noise.getValue(blockX / 100.0, 0, blockZ / 100.0),
               noiseZ = noise.getValue(blockX / 100.0, 100, blockZ / 100.0);
        double gradientFactor = 1;
        if (gradient < 0.25) {
            gradientFactor = gradient * 4;
        } else if (gradient > 0.75) {
            gradientFactor = (1 - gradient) * 4;
        }
        gradientFactor = Mth.smoothstep(gradientFactor);

        double lineSegmentDistance = Math.sqrt(sdSegment(
                posInWatershed.x + noiseX * 0.6 * gradientFactor, posInWatershed.z() + noiseZ * 0.6 * gradientFactor,
                sourcePos.x, sourcePos.z(),
                drainPos.x, drainPos.z())
        ) * WATERSHED_SIZE * 0.5;


        //if (blockY < waterLevel) lineSegmentDistance *= 1.5;
        // add a bit of a shore
        lineSegmentDistance *= Mth.clampedMap(blockY - waterLevel, 0, 1.5, 2, 1) * 0.4;

        double noiseVal = noise.getValue(blockX / 8.0, blockY / 32.0, blockZ / 8.0);

        return Math.sqrt(lineSegmentDistance * lineSegmentDistance + surfaceDistance * surfaceDistance) + noiseVal * 3;
    }

    public static double sdSegment(double px, double py,
                                   double ax, double ay,
                                   double bx, double by) {
        double bax = bx - ax, bay = by - ay;
        double h = ((px - ax) * bax + (py - ay) * bay) / (bax * bax + bay * bay);
        if (h < 0) h = 0;
        else if (h > 1) h = 1;
        double dx = (px - ax) - bax * h;
        double dy = (py - ay) - bay * h;
        return dx * dx + dy * dy;
    }

    private static double terrace(double x, double frequency, double gradient) {
        double xFloor = Math.floor(x / frequency) * frequency;
        double xMod = (x - xFloor) / frequency;

        if (xMod < 1 - gradient)
            xMod = 0;
        else
            xMod = Mth.smoothstep(Mth.map(xMod, 1 - gradient, 1, 0, 1));

        return xMod * frequency + xFloor;
    }

    @Override
    public double compute(FunctionContext context) {
//        double distanceX = (context.blockX() % 30) - 15;
//        double distanceY = context.blockY() - 15;
//        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
//        return Mth.clamp(Mth.map(distance - 5, -10, 10, -1, 1), -100, 100);
        double distance = riverDistance(context.blockX(), context.blockY(), context.blockZ());
        return Mth.clamp(Mth.map(distance - 5, -10, 10, -1, 1), -100, 100);
    }

    @Override
    public double minValue() {
        return -100;
    }

    @Override
    public double maxValue() {
        return 100;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    private record WatershedPosition(double x, double z) {}
}
