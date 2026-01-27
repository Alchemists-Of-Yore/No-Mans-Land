package com.farcr.nomansland.common.registry.entities;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.effect.FlammableEffect;
import com.farcr.nomansland.common.effect.FriendshipEffect;
import com.farcr.nomansland.common.effect.PacifiedEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NMLEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, NoMansLand.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> FLAMMABLE = MOB_EFFECTS.register("flammable",
            () -> new FlammableEffect(MobEffectCategory.NEUTRAL, 4796183));

    public static final DeferredHolder<MobEffect, MobEffect> PACIFIED = MOB_EFFECTS.register("pacified",
            () -> new PacifiedEffect(MobEffectCategory.NEUTRAL));

    public static final DeferredHolder<MobEffect, MobEffect> FRIENDSHIP = MOB_EFFECTS.register("friendship",
        () -> new FriendshipEffect(MobEffectCategory.NEUTRAL, 7637070));
}
