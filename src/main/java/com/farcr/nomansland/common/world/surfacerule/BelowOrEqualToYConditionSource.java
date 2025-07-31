package com.farcr.nomansland.common.world.surfacerule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public record BelowOrEqualToYConditionSource(VerticalAnchor anchor, boolean useSurfacePosition, boolean addNoise) implements SurfaceRules.ConditionSource {
    public static final KeyDispatchDataCodec<BelowOrEqualToYConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                    record -> record.group(
                            VerticalAnchor.CODEC.fieldOf("anchor").forGetter(BelowOrEqualToYConditionSource::anchor),
                            Codec.BOOL.fieldOf("use_surface_position").forGetter(BelowOrEqualToYConditionSource::useSurfacePosition),
                            Codec.BOOL.fieldOf("add_noise").forGetter(BelowOrEqualToYConditionSource::addNoise)
                            ).apply(record, BelowOrEqualToYConditionSource::new)
            )
    );

    public SurfaceRules.Condition apply(final SurfaceRules.Context surfaceRulesContext) {
        return new YCondition(surfaceRulesContext, this.anchor, this.useSurfacePosition, this.addNoise);
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }

    static class YCondition extends SurfaceRules.LazyYCondition {
        private final VerticalAnchor anchor;
        private final boolean useSurfacePosition;
        private final boolean addSurfaceNoise;

        YCondition(SurfaceRules.Context surfaceRulesContext, VerticalAnchor anchor, boolean useSurfacePosition, boolean addSurfaceNoise) {
            super(surfaceRulesContext);
            this.anchor = anchor;
            this.useSurfacePosition = useSurfacePosition;
            this.addSurfaceNoise = addSurfaceNoise;
        }

        @Override
        protected boolean compute() {
            int y = context.blockY;
            if (useSurfacePosition)
                y += context.stoneDepthAbove;
            if (addSurfaceNoise)
                y += context.surfaceDepth;
            return y <= this.anchor.resolveY(this.context.context);
        }
    }
}
