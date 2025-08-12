package com.farcr.nomansland.common.world.densityfunction;

import com.farcr.nomansland.utility.MathUtilities;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public final class SmoothMixDensityFunction implements DensityFunction {
    private static final MapCodec<SmoothMixDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(
            record -> record.group(
                    Type.CODEC.fieldOf("type").forGetter(densityFunction -> densityFunction.type),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter(densityFunction -> densityFunction.argument1),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter(densityFunction -> densityFunction.argument2),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("smoothness").forGetter(densityFunction -> densityFunction.smoothness)
            ).apply(record, SmoothMixDensityFunction::new)
    );
    public static final KeyDispatchDataCodec<SmoothMixDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    public static DensityFunction create(Type type, DensityFunction argument1, DensityFunction argument2, DensityFunction smoothness) {
        if (smoothness instanceof DensityFunctions.Constant constant && constant.value() == 0) {
            return switch (type) {
                case MIN -> DensityFunctions.min(argument1, argument2);
                case MAX -> DensityFunctions.max(argument1, argument2);
            };
        }
        return new SmoothMixDensityFunction(type, argument1, argument2, smoothness);
    }

    private final Type type;
    private final DensityFunction argument1;
    private final DensityFunction argument2;
    private final DensityFunction smoothness;

    private SmoothMixDensityFunction(Type type, DensityFunction argument1, DensityFunction argument2, DensityFunction smoothness) {
        this.type = type;
        this.argument1 = argument1;
        this.argument2 = argument2;
        this.smoothness = smoothness;
    }

    @Override
    public double compute(FunctionContext context) {
        double value1 = this.argument1.compute(context);
        double value2 = this.argument2.compute(context);
        double smoothness = this.smoothness.compute(context);
        return switch (this.type) {
            case MIN -> MathUtilities.smoothMin(value1, value2, smoothness);
            case MAX -> -MathUtilities.smoothMin(-value1, -value2, smoothness);
        };
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        this.argument1.fillArray(array, contextProvider);
        switch (this.type) {
            case MIN:
                for (int i = 0; i < array.length; i++)
                    array[i] = MathUtilities.smoothMin(array[i], this.argument2.compute(contextProvider.forIndex(i)), this.smoothness.compute(contextProvider.forIndex(i)));
                break;
            case MAX:
                for (int i = 0; i < array.length; i++)
                    array[i] = -MathUtilities.smoothMin(-array[i], -this.argument2.compute(contextProvider.forIndex(i)), this.smoothness.compute(contextProvider.forIndex(i)));
                break;
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new SmoothMixDensityFunction(
                this.type,
                this.argument1.mapAll(visitor),
                this.argument2.mapAll(visitor),
                this.smoothness.mapAll(visitor)
        ));
    }

    @Override
    public double minValue() {
        return Math.min(this.argument1.minValue(), this.argument2.minValue());
    }

    @Override
    public double maxValue() {
        return Math.max(this.argument1.maxValue(), this.argument2.maxValue());
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    public enum Type implements StringRepresentable {
        MIN("min"), MAX("max");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
