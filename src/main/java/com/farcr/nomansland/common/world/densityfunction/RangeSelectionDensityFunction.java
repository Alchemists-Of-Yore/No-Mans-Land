package com.farcr.nomansland.common.world.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public record RangeSelectionDensityFunction(DensityFunction input, double min, double max, double gradient) implements DensityFunctions.PureTransformer {
    private static final MapCodec<RangeSelectionDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(
            record -> record.group(
                            DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(RangeSelectionDensityFunction::input),
                            Codec.doubleRange(-1000000.0, 1000000.0).fieldOf("min").forGetter(RangeSelectionDensityFunction::min),
                            Codec.doubleRange(-1000000.0, 1000000.0).fieldOf("max").forGetter(RangeSelectionDensityFunction::max),
                            Codec.doubleRange(-1000000.0, 1000000.0).fieldOf("gradient").forGetter(RangeSelectionDensityFunction::gradient)
                    )
                    .apply(record, RangeSelectionDensityFunction::new)
    );
    public static final KeyDispatchDataCodec<RangeSelectionDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double transform(double value) {
        if (value < min + gradient) return Mth.clampedMap(value, min, min + gradient, 0, 1);
        if (value > max - gradient) return Mth.clampedMap(value, max - gradient, max, 1, 0);
        return 1;
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return 1;
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new RangeSelectionDensityFunction(this.input.mapAll(visitor), min, max, gradient);
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
