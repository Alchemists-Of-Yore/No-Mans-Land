package com.farcr.nomansland.common.world.feature.placementmodifier;

import com.farcr.nomansland.common.registry.worldgen.NMLPlacementModifiers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/*
     "height_filter" placement type!

     removes placements that don't fall between a minimum and maximum height.

     EXAMPLE SYNTAX:
       {
          "type": "nomansland:height_filter",
          "height_min": 0,  // integer. defaults to -1000000. (no minimum)
                                        inclusive. the minimum height that a position isn't filtered.
          "height_max": 12, // integer. defaults to 1000000. (no maximum)
                                        inclusive. the maximum height that a position isn't filtered.
       }
*/
public class HeightFilterPlacement extends PlacementFilter {
    public static final MapCodec<HeightFilterPlacement> CODEC = RecordCodecBuilder.mapCodec (
            codec -> codec.group(
                            Codec.INT.fieldOf("height_min").orElse(-1000000).forGetter(instance -> instance.minimumHeight),
                            Codec.INT.fieldOf("height_max").orElse( 1000000).forGetter(instance -> instance.maximumHeight)
                    ).apply(codec, HeightFilterPlacement::new)
    );

    private final int minimumHeight, maximumHeight;

    public HeightFilterPlacement(int minimumHeight, int maximumHeight) {
        this.minimumHeight = minimumHeight;
        this.maximumHeight = maximumHeight;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return pos.getY() >= minimumHeight && pos.getY() <= maximumHeight;
    }

    @Override
    public PlacementModifierType<?> type() {
        return NMLPlacementModifiers.HEIGHT_FILTER.get();
    }
}
