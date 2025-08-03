package com.farcr.nomansland.common.world.surfacerule;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record AndConditionSource(SurfaceRules.ConditionSource target1, SurfaceRules.ConditionSource target2) implements SurfaceRules.ConditionSource {
    public static final KeyDispatchDataCodec<AndConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                    record -> record.group(
                            SurfaceRules.ConditionSource.CODEC.fieldOf("target_1").forGetter(AndConditionSource::target1),
                            SurfaceRules.ConditionSource.CODEC.fieldOf("target_2").forGetter(AndConditionSource::target2)
                    ).apply(record, AndConditionSource::new)
            )
    );

    public SurfaceRules.Condition apply(final SurfaceRules.Context context) {
        return new AndCondition(target1.apply(context), target2.apply(context));
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }

    record AndCondition(SurfaceRules.Condition target1, SurfaceRules.Condition target2) implements SurfaceRules.Condition {
        @Override
        public boolean test() {
            return this.target1.test() && this.target2.test();
        }
    }
}
