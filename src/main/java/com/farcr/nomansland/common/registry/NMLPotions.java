package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NMLPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, NoMansLand.MODID);

    public static final Holder<Potion> CORROSION = POTIONS.register("corrosion",
            () -> new Potion("corrosion", new MobEffectInstance(NMLEffects.CORROSION, 160, 0)));
    public static final Holder<Potion> STRONG_CORROSION = POTIONS.register("strong_corrosion",
            () -> new Potion("corrosion", new MobEffectInstance(NMLEffects.CORROSION, 280, 1)));
}
