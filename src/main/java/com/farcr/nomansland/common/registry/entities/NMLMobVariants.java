package com.farcr.nomansland.common.registry.entities;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.billhook_bass.BillhookBassVariant;
import com.farcr.nomansland.common.entity.deer.DeerAntlersVariant;
import com.farcr.nomansland.common.entity.deer.DeerPatternVariant;
import com.farcr.nomansland.common.entity.deer.DeerVariant;
import com.farcr.nomansland.common.entity.goose.GooseVariant;
import com.farcr.nomansland.common.entity.tortoise.TortoiseVariant;
import com.mojang.serialization.MapCodec;
import dev.tazer.mixed_litter.MLRegistries;
import dev.tazer.mixed_litter.variants.MobVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.FrogVariant;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class NMLMobVariants {
    public static final DeferredRegister<FrogVariant> FROG_VARIANTS = DeferredRegister.create(BuiltInRegistries.FROG_VARIANT, NoMansLand.MODID);
    public static final DeferredHolder<FrogVariant, FrogVariant> MUD = FROG_VARIANTS.register("mud", () -> new FrogVariant(NoMansLand.location("textures/entity/mob_variants/frog/mud_frog.png")));

    public static final DeferredRegister<MapCodec<? extends MobVariant>> MOB_VARIANT_TYPES = DeferredRegister.create(MLRegistries.ANIMAL_VARIANT_TYPE, NoMansLand.MODID);
    public static final Supplier<MapCodec<DeerVariant>> DEER = register("deer", DeerVariant.CODEC);
    public static final Supplier<MapCodec<DeerAntlersVariant>> DEER_ANTLERS = register("deer_antlers", DeerAntlersVariant.CODEC);
    public static final Supplier<MapCodec<DeerPatternVariant>> DEER_PATTERN = register("deer_pattern", DeerPatternVariant.CODEC);
    public static final Supplier<MapCodec<BillhookBassVariant>> BILLHOOK_BASS = register("billhook_bass", BillhookBassVariant.CODEC);
    public static final Supplier<MapCodec<GooseVariant>> GOOSE = register("goose", GooseVariant.CODEC);
    public static final Supplier<MapCodec<TortoiseVariant>> TORTOISE = register("tortoise", TortoiseVariant.CODEC);

    private static <P extends MobVariant> DeferredHolder<MapCodec<? extends MobVariant>, MapCodec<P>> register(String name, MapCodec<P> codec) {
        return MOB_VARIANT_TYPES.register(name, () -> codec);
    }
}
