package com.farcr.nomansland.common.world.surfacerule;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public record BelowOrEqualToYConditionSource(VerticalAnchor anchor) implements SurfaceRules.ConditionSource {
    public static final KeyDispatchDataCodec<BelowOrEqualToYConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                    record -> record.group(
                            VerticalAnchor.CODEC.fieldOf("anchor").forGetter(BelowOrEqualToYConditionSource::anchor)
                            ).apply(record, BelowOrEqualToYConditionSource::new)
            )
    );

    public SurfaceRules.Condition apply(final SurfaceRules.Context surfaceRulesContext) {
        class StupidInternalClassConditionThatIHateCondition extends SurfaceRules.LazyYCondition {
            StupidInternalClassConditionThatIHateCondition() {
                super(surfaceRulesContext);
            }
            @Override
            protected boolean compute() {
                return context.blockY <= BelowOrEqualToYConditionSource.this.anchor.resolveY(this.context.context);
            }
        }
        return new StupidInternalClassConditionThatIHateCondition();
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }
}
