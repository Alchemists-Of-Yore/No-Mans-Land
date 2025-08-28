package com.farcr.nomansland.common.registry.entities;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.FrogVariant;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class NMLMobVariants {
    public static final DeferredRegister<FrogVariant> FROG_VARIANTS = DeferredRegister.create(BuiltInRegistries.FROG_VARIANT, NoMansLand.MODID);
    public static final DeferredHolder<FrogVariant, FrogVariant> MUD = FROG_VARIANTS.register("mud", () -> new FrogVariant(NoMansLand.location("textures/entity/mob_variants/frog/mud_frog.png")));
}
