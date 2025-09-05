package com.farcr.nomansland.common.world.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

/*
  "range_select" density function!
  makes everything between min and max 1, and everything else 0,
  with gradient being the size of the transition.

  EXAMPLE SYNTAX:
  {
      // the minimum of the selected range. a number.
      "min": 0.5,
      // the maximum of the selected range. a number.
      "max": 0.8,
      // how large the transition between 0 and 1 is. a number
      "gradient": 0.1,
      // the input density function, to select the range from.
      "input": {
        *INSERT ANOTHER DENSITY FUNCTION HERE*
      }
  }
 */
public record RangeSelectDensityFunction(DensityFunction input, double min, double max, double gradient) implements DensityFunctions.PureTransformer {
    private static final MapCodec<RangeSelectDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(
            record -> record.group(
                            DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(RangeSelectDensityFunction::input),
                            Codec.doubleRange(-1000000.0, 1000000.0).fieldOf("min").forGetter(RangeSelectDensityFunction::min),
                            Codec.doubleRange(-1000000.0, 1000000.0).fieldOf("max").forGetter(RangeSelectDensityFunction::max),
                            Codec.doubleRange(-1000000.0, 1000000.0).fieldOf("gradient").forGetter(RangeSelectDensityFunction::gradient)
                    )
                    .apply(record, RangeSelectDensityFunction::new)
    );
    public static final KeyDispatchDataCodec<RangeSelectDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

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
        return visitor.apply(new RangeSelectDensityFunction(this.input.mapAll(visitor), min, max, gradient));
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
