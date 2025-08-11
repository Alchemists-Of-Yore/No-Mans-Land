package com.farcr.nomansland.common.world.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

/*
  "remap" density function!
  a map range function. remaps the range from in_min -> in_max to out_min -> out_max

  EXAMPLE SYNTAX:
  {
      // the minimum of the input range. gets transformed to out_min
      "in_min": 0,
      // the maximum of the input range. gets transformed to out_max
      "in_max": 1,
      // the minimum of the output range
      "out_min": 5,
      // the maximum of the output range
      "out_max": 15,
      // whether to clamp the result between out_min and out_max.
      "clamp" : true,
      "input": {
        *INSERT ANOTHER DENSITY FUNCTION HERE*
      }
  }*/
public record RemapDensityFunction(DensityFunction input, double inMin, double inMax, double outMin, double outMax, boolean clamp) implements DensityFunctions.PureTransformer {
    private static final MapCodec<RemapDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(
            record -> record.group(
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(RemapDensityFunction::input),
                            Codec.DOUBLE.fieldOf("in_min").forGetter(RemapDensityFunction::inMin),
                            Codec.DOUBLE.fieldOf("in_max").forGetter(RemapDensityFunction::inMax),
                            Codec.DOUBLE.fieldOf("out_min").forGetter(RemapDensityFunction::outMin),
                            Codec.DOUBLE.fieldOf("out_max").forGetter(RemapDensityFunction::outMax),
                            Codec.BOOL.fieldOf("clamp").forGetter(RemapDensityFunction::clamp)
                    )
                    .apply(record, RemapDensityFunction::new)
    );
    public static final KeyDispatchDataCodec<RemapDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double transform(double value) {
        return clamp ? Mth.clampedMap(value, inMin, inMax, outMin, outMax) : Mth.map(value, inMin, inMax, outMin, outMax);
    }

    @Override
    public double minValue() {
        return clamp ? outMin : Mth.map(input.minValue(), inMin, inMax, outMin, outMax);
    }

    @Override
    public double maxValue() {
        return clamp ? outMax : Mth.map(input.maxValue(), inMin, inMax, outMin, outMax);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new RemapDensityFunction(this.input.mapAll(visitor), inMin, inMax, outMin, outMax, clamp));
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
