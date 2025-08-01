package com.farcr.nomansland.common.world.surfacerule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public record BelowOrEqualToYConditionSource(VerticalAnchor anchor, boolean useSurfacePosition, boolean addNoise, float offset) implements SurfaceRules.ConditionSource {
    public static final KeyDispatchDataCodec<BelowOrEqualToYConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                    record -> record.group(
                            VerticalAnchor.CODEC.fieldOf("anchor").forGetter(BelowOrEqualToYConditionSource::anchor),
                            Codec.BOOL.fieldOf("use_surface_position").forGetter(BelowOrEqualToYConditionSource::useSurfacePosition),
                            Codec.BOOL.fieldOf("add_noise").forGetter(BelowOrEqualToYConditionSource::addNoise),
                            Codec.FLOAT.fieldOf("offset").orElse(0.0F).forGetter(BelowOrEqualToYConditionSource::offset)
                    ).apply(record, BelowOrEqualToYConditionSource::new)
            )
    );

    public SurfaceRules.Condition apply(final SurfaceRules.Context surfaceRulesContext) {
        return new YCondition(surfaceRulesContext, this.anchor, this.useSurfacePosition, this.addNoise, this.offset);
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }

    static class YCondition extends SurfaceRules.LazyYCondition {
        private final VerticalAnchor anchor;
        private final boolean useSurfacePosition;
        private final boolean addSurfaceNoise;
        private final float offset;

        YCondition(SurfaceRules.Context surfaceRulesContext, VerticalAnchor anchor, boolean useSurfacePosition, boolean addSurfaceNoise, float offset) {
            super(surfaceRulesContext);
            this.anchor = anchor;
            this.useSurfacePosition = useSurfacePosition;
            this.addSurfaceNoise = addSurfaceNoise;
            this.offset = offset;
        }

        @Override
        protected boolean compute() {
            float y = context.blockY + this.offset;
            if (useSurfacePosition)
                y += context.stoneDepthAbove;
            if (addSurfaceNoise)
                y += context.surfaceDepth;
            return y <= this.anchor.resolveY(this.context.context);
        }
    }
}
