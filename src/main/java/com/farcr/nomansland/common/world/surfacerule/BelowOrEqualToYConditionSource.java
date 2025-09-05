package com.farcr.nomansland.common.world.surfacerule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public record BelowOrEqualToYConditionSource(VerticalAnchor anchor, boolean useSurfacePosition, float noiseMultiplier, float offset) implements SurfaceRules.ConditionSource {
    public static final KeyDispatchDataCodec<BelowOrEqualToYConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                    record -> record.group(
                            VerticalAnchor.CODEC.fieldOf("anchor").forGetter(BelowOrEqualToYConditionSource::anchor),
                            Codec.BOOL.fieldOf("use_surface_position").forGetter(BelowOrEqualToYConditionSource::useSurfacePosition),
                            Codec.FLOAT.fieldOf("noise_multiplier").forGetter(BelowOrEqualToYConditionSource::noiseMultiplier),
                            Codec.FLOAT.fieldOf("offset").orElse(0.0F).forGetter(BelowOrEqualToYConditionSource::offset)
                    ).apply(record, BelowOrEqualToYConditionSource::new)
            )
    );

    public SurfaceRules.Condition apply(final SurfaceRules.Context surfaceRulesContext) {
        return new YCondition(surfaceRulesContext, this.anchor, this.useSurfacePosition, this.noiseMultiplier, this.offset);
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }

    static class YCondition extends SurfaceRules.LazyYCondition {
        private final VerticalAnchor anchor;
        private final boolean useSurfacePosition;
        private final float surfaceNoiseMultiplier;
        private final float offset;

        YCondition(SurfaceRules.Context surfaceRulesContext, VerticalAnchor anchor, boolean useSurfacePosition, float surfaceNoiseMultiplier, float offset) {
            super(surfaceRulesContext);
            this.anchor = anchor;
            this.useSurfacePosition = useSurfacePosition;
            this.surfaceNoiseMultiplier = surfaceNoiseMultiplier;
            this.offset = offset;
        }

        @Override
        protected boolean compute() {
            float y = context.blockY + this.offset;
            if (useSurfacePosition)
                y += context.stoneDepthAbove;
            y += ((context.surfaceDepth - 2.5F) / 2.5F) * surfaceNoiseMultiplier;
            return y + offset <= this.anchor.resolveY(this.context.context);
        }
    }
}
