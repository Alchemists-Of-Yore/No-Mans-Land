package com.farcr.nomansland.common.world.densityfunction;

import com.farcr.nomansland.common.world.watershed.River;
import com.farcr.nomansland.common.world.watershed.Watershed;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record CaveRiverDistanceDensityFunction(boolean horizontal, WatershedMap watershedMap) implements DensityFunction.SimpleFunction {
    public static final KeyDispatchDataCodec<CaveRiverDistanceDensityFunction> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new CaveRiverDistanceDensityFunction(false, null))
    );

    @Override
    public double compute(FunctionContext context) {
        Watershed watershed = this.watershedMap.watershedAtBlock(context.blockX(), context.blockZ());
        River.RiverSpaceCoordinates river = watershed.river().getRiverSpaceCoordinates(context.blockX(), context.blockY(), context.blockZ());
        return horizontal ? Math.clamp(river.horizontalDistance(), 0, 100) : river.riverHeight();
    }

    @Override
    public double minValue() {
        return horizontal ? 0 : -10000;
    }

    @Override
    public double maxValue() {
        return horizontal ? 100 : 10000;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
