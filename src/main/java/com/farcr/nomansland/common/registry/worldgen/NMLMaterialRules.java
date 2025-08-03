package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.surfacerule.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLMaterialRules {
    public static final DeferredRegister<MapCodec<? extends SurfaceRules.RuleSource>> MATERIAL_RULES = DeferredRegister.create(Registries.MATERIAL_RULE, NoMansLand.MODID);

    public static final Supplier<MapCodec<? extends SurfaceRules.RuleSource>> STRATA =
            MATERIAL_RULES.register("strata", StrataRuleSource.CODEC::codec);
}
