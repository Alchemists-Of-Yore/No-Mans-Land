package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamtypes.MoonlightDreamType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLDreamTypes {
    public static final DeferredRegister<DreamType> DREAM_TYPES_REGISTRY =
        DeferredRegister.create(NMLRegistries.DREAM_TYPE, NoMansLand.MODID);

    public static final Supplier<DreamType> FRIEND_MOON_DREAM =
        DREAM_TYPES_REGISTRY.register("friend_moon_dream", MoonlightDreamType::new);
}
