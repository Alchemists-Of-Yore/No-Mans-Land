package com.farcr.nomansland.common.world.orevein;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

import java.util.Optional;

/*
    put all ore vein jsons in resources/data/nomansland/nomansland/worldgen/ore_vein/
    EXAMPLE JSON:

    {
      // determines the biomes that the vein's center is allowed to be in.
      // a vein may spread well outside its biome! so, be careful...
      // can be a tag or a list of biomes.
      // optional. if left blank, the vein may spawn in any biome.
      "biomes": "#minecraft:mineshaft_blocking",

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

      // "radius" of an individual ore vein
      // or, if inverted is true, the radius of the "bubbles" formed between the veins.
      // if inverted is true, then the value should probably be larger...
      // reasonable range = 3 to about 64, but can theoretically be any positive number
      // defaults to 5.12
      "vein_radius": {
        "type": "constant",
        "value": 5.12
      },
      // whether the ore veins should place in, kind of like, a vein-y manor, or a bubbly manor...
      // idk exactly how to describe this one. just try it and see
      // defaults to false
      "inverted": false,

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
public record OreVeinType(Optional<HolderSet<Biome>> biomes,
                          int spacing, int separation, float probability,
                          IntProvider radius,
                          HeightProvider minHeight, HeightProvider maxHeight,
                          FloatProvider veinRadius, boolean invert,
                          OreBlockState filler,
                          OreBlockState ore,
                          OreBlockState raw) {
    public static final Codec<OreVeinType> CODEC = RecordCodecBuilder.create(
            codec -> codec.group(
                    RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(OreVeinType::biomes),
                    Codec.INT.fieldOf("spacing").validate(
                            (spacing) -> (spacing % 16 != 0) || (spacing <= 0) ? DataResult.error(() -> "Spacing must be positive and multiple of 16! Currently " + spacing) : DataResult.success(spacing)
                    ).forGetter(OreVeinType::spacing),
                    Codec.INT.fieldOf("separation").validate(
                            (separation) -> separation <= 0 ? DataResult.error(() -> "Separation must be positive!") : DataResult.success(separation)
                    ).forGetter(OreVeinType::separation),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(OreVeinType::probability),
                    IntProvider.CODEC.fieldOf("radius").forGetter(OreVeinType::radius),
                    HeightProvider.CODEC.fieldOf("min_height").forGetter(OreVeinType::minHeight),
                    HeightProvider.CODEC.fieldOf("max_height").forGetter(OreVeinType::maxHeight),
                    FloatProvider.codec(0, 10000000.0F).fieldOf("vein_radius").orElse(ConstantFloat.of(5.12F)).forGetter(OreVeinType::veinRadius),
                    Codec.BOOL.fieldOf("invert").orElse(false).forGetter(OreVeinType::invert),
                    OreBlockState.CODEC.fieldOf("filler").forGetter(OreVeinType::filler),
                    OreBlockState.CODEC.fieldOf("ore").forGetter(OreVeinType::ore),
                    OreBlockState.CODEC.fieldOf("raw").forGetter(OreVeinType::raw)
            ).apply(codec, OreVeinType::new)
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
