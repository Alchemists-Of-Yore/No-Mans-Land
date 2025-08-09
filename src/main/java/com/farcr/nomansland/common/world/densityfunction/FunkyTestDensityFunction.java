package com.farcr.nomansland.common.world.densityfunction;

import com.farcr.nomansland.NoMansLand;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record FunkyTestDensityFunction(DensityFunction input) implements DensityFunction {
    private static final MapCodec<FunkyTestDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(
            record -> record.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(FunkyTestDensityFunction::input))
                    .apply(record, FunkyTestDensityFunction::new)
    );
    public static final KeyDispatchDataCodec<FunkyTestDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    private double transform(double value, int x, int y, int z) {
        int roundedX = x / 50;
        int roundedZ = z / 50;
        if (Math.floorMod(roundedX + roundedZ, 2) > 0) {
            return value + 0.1;
        }
        return value - 0.1;
    }

    @Override
    public double compute(FunctionContext context) {
        return transform(input.compute(context), context.blockX(), context.blockX(), context.blockZ());
    }

    @Override
    public void fillArray(double[] array, DensityFunction.ContextProvider contextProvider) {
        this.input().fillArray(array, contextProvider);

        for (int i = 0; i < array.length; i++) {
            FunctionContext context = contextProvider.forIndex(i);
            array[i] = this.transform(array[i], context.blockX(), context.blockX(), context.blockZ());
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new FunkyTestDensityFunction(input.mapAll(visitor)));
    }

    @Override
    public double minValue() {
        return input.minValue() - 0.5;
    }

    @Override
    public double maxValue() {
        return input.maxValue() + 0.5;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
