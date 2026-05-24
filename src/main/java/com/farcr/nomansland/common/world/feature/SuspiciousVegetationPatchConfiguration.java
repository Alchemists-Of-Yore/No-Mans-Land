package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Optional;

public class SuspiciousVegetationPatchConfiguration extends VegetationPatchConfiguration {
    public static final Codec<SuspiciousVegetationPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("replaceable").forGetter(c -> c.replaceable),
            BlockStateProvider.CODEC.fieldOf("ground_state").forGetter(c -> c.groundState),
            PlacedFeature.CODEC.fieldOf("vegetation_feature").forGetter(c -> c.vegetationFeature),
            CaveSurface.CODEC.fieldOf("surface").forGetter(c -> c.surface),
            IntProvider.codec(1, 128).fieldOf("depth").forGetter(c -> c.depth),
            Codec.floatRange(0.0F, 1.0F).fieldOf("extra_bottom_block_chance").forGetter(c -> c.extraBottomBlockChance),
            Codec.intRange(1, 256).fieldOf("vertical_range").forGetter(c -> c.verticalRange),
            Codec.floatRange(0.0F, 1.0F).fieldOf("vegetation_chance").forGetter(c -> c.vegetationChance),
            IntProvider.CODEC.fieldOf("xz_radius").forGetter(c -> c.xzRadius),
            Codec.floatRange(0.0F, 1.0F).fieldOf("extra_edge_column_chance").forGetter(c -> c.extraEdgeColumnChance),
            BlockState.CODEC.fieldOf("suspicious_state").forGetter(c -> c.suspiciousState),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("suspicious_chance", 0.1F).forGetter(c -> c.suspiciousChance),
            ResourceLocation.CODEC.optionalFieldOf("loot_table").forGetter(c -> c.lootTable)
    ).apply(instance, SuspiciousVegetationPatchConfiguration::new));

    public final BlockState suspiciousState;
    public final float suspiciousChance;
    public final Optional<ResourceLocation> lootTable;

    public SuspiciousVegetationPatchConfiguration(
            TagKey<Block> replaceable,
            BlockStateProvider groundState,
            Holder<PlacedFeature> vegetationFeature,
            CaveSurface surface,
            IntProvider depth,
            float extraBottomBlockChance,
            int verticalRange,
            float vegetationChance,
            IntProvider xzRadius,
            float extraEdgeColumnChance,
            BlockState suspiciousState,
            float suspiciousChance,
            Optional<ResourceLocation> lootTable
    ) {
        super(replaceable, groundState, vegetationFeature, surface, depth, extraBottomBlockChance, verticalRange, vegetationChance, xzRadius, extraEdgeColumnChance);
        this.suspiciousState = suspiciousState;
        this.suspiciousChance = suspiciousChance;
        this.lootTable = lootTable;
    }
}
