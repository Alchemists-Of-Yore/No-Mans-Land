package com.farcr.nomansland.common.world.orevein;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

/*
    put all ore vein jsons in resources/data/nomansland/nomansland/worldgen/ore_vein/
    EXAMPLE JSON:

    {
      // for spacing and separation, see https://minecraft.wiki/w/Bastion_Remnant#/media/File:Java_Nether_Structure_Generation1.png
      // the maximum distance between ore veins (the size of a grid cell on that graph)
      // note: MUST BE A MULTIPLE OF SIXTEEN!
      "spacing": 256,
      // the minimum distance between ore veins (the red area on that graph)
      "separation": 32,

      // the probability that a ore vein will be discarded completely
      "probability": 1.0,

      // the radius of the ore vein. an IntProvider
      "radius": {
        "type": "uniform",
        "min_inclusive": 32,
        "max_inclusive": 128
      },

      // for min and max height, if max height is less than min height, then the ore vein will not generate.
      // determines the lower bound of the ore vein. a HeightProvider.
      "min_height": {
        "type": "constant",
        "value": {
          "above_bottom": 30
        }
      },
      // determines the upper bound of the ore vein. a HeightProvider.
      "max_height": {
        "type": "constant",
        "value": {
          "above_bottom": 64
        }
      },

      // the blocks that make up the vein.
      // "ore" is placed when y > 0, and "deepslate ore" is placed when y < 0.
      // these can be the same block.
      "filler": {
        "ore": { "Name": "minecraft:tuff" },
        "deepslate_ore": { "Name": "minecraft:tuff" }
      },
      "ore": {
        "ore": { "Name": "minecraft:iron_ore" },
        "deepslate_ore": { "Name": "minecraft:deepslate_iron_ore" }
      },
      "raw": {
        "ore": { "Name": "minecraft:raw_iron_block" },
        "deepslate_ore": { "Name": "minecraft:raw_iron_block" }
      }
    }
 */
public record OreVein(int spacing, int separation, float probability,
                      IntProvider radius,
                      HeightProvider minHeight, HeightProvider maxHeight,
                      OreBlockState filler,
                      OreBlockState ore,
                      OreBlockState raw) {
    public static final Codec<OreVein> CODEC = RecordCodecBuilder.create(
            codec -> codec.group(
                    Codec.INT.fieldOf("spacing").validate(
                            (spacing) -> {
                                if (spacing % 16 != 0) return DataResult.error(() -> "Spacing must be multiple of 16!");
                                if (spacing <= 0) return DataResult.error(() -> "Spacing must be positive!");
                                return DataResult.success(spacing);
                            }
                    ).forGetter(OreVein::spacing),
                    Codec.INT.fieldOf("separation").forGetter(OreVein::separation),
                    Codec.FLOAT.fieldOf("probability").forGetter(OreVein::probability),
                    IntProvider.CODEC.fieldOf("radius").forGetter(OreVein::radius),
                    HeightProvider.CODEC.fieldOf("min_height").forGetter(OreVein::minHeight),
                    HeightProvider.CODEC.fieldOf("max_height").forGetter(OreVein::maxHeight),
                    OreBlockState.CODEC.fieldOf("filler").forGetter(OreVein::filler),
                    OreBlockState.CODEC.fieldOf("ore").forGetter(OreVein::ore),
                    OreBlockState.CODEC.fieldOf("raw").forGetter(OreVein::raw)
            ).apply(codec, OreVein::new)
    );

    public record OreBlockState(BlockState ore, BlockState deepslateOre) {
        public static final Codec<OreBlockState> CODEC = RecordCodecBuilder.create(
                codec -> codec.group(
                        BlockState.CODEC.fieldOf("ore").forGetter(OreBlockState::ore),
                        BlockState.CODEC.fieldOf("deepslate_ore").forGetter(OreBlockState::deepslateOre)
                ).apply(codec, OreBlockState::new)
        );

        public BlockState resolve(int y) {
            return y > 0 ? ore : deepslateOre;
        }
    }
}
