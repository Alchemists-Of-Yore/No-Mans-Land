package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.surfacerule.AndConditionSource;
import com.farcr.nomansland.common.world.surfacerule.BelowOrEqualToYConditionSource;
import com.farcr.nomansland.common.world.surfacerule.BiomeTagConditionSource;
import com.farcr.nomansland.common.world.surfacerule.OrConditionSource;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLMaterialConditions {
    public static final DeferredRegister<MapCodec<? extends SurfaceRules.ConditionSource>> MATERIAL_CONDITIONS = DeferredRegister.create(Registries.MATERIAL_CONDITION, NoMansLand.MODID);

    public static final Supplier<MapCodec<? extends SurfaceRules.ConditionSource>> BELOW_OR_EQUAL_TO_Y =
            MATERIAL_CONDITIONS.register("below_or_equal_to_y", BelowOrEqualToYConditionSource.CODEC::codec);

    public static final Supplier<MapCodec<? extends SurfaceRules.ConditionSource>> BIOME_TAG =
            MATERIAL_CONDITIONS.register("biome_tag", BiomeTagConditionSource.CODEC::codec);

    public static final Supplier<MapCodec<? extends SurfaceRules.ConditionSource>> AND =
            MATERIAL_CONDITIONS.register("and", AndConditionSource.CODEC::codec);

    public static final Supplier<MapCodec<? extends SurfaceRules.ConditionSource>> OR =
            MATERIAL_CONDITIONS.register("or", OrConditionSource.CODEC::codec);
}
