package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.ambience.fogmodifiers.*;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLFogModifiers {
    public static final DeferredRegister<FogModifier> FOG_MODIFIERS = DeferredRegister.create(NMLRegistries.FOG_MODIFIERS, NoMansLand.MODID);

    public static final Supplier<FogLooksGoodNowModifier> FOG_LOOKS_GOOD_NOW_MODIFIER = FOG_MODIFIERS.register("flgn_modifier", FogLooksGoodNowModifier::new);
    public static final Supplier<FoggyBiomeFogModifier> FOGGY_BIOME_MODIFIER = FOG_MODIFIERS.register("foggy_biome_modifier", FoggyBiomeFogModifier::new);
    public static final Supplier<DeepDarkFogModifier> DEEP_DARK_MODIFIER = FOG_MODIFIERS.register("deep_dark_modifier", DeepDarkFogModifier::new);
    public static final Supplier<CaveBiomeFogModifier> CAVE_BIOME_MODIFIER = FOG_MODIFIERS.register("cave_biome_modifier", CaveBiomeFogModifier::new);
}
