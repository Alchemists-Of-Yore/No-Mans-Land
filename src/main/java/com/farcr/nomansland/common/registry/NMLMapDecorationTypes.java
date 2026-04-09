package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NMLMapDecorationTypes {
    public static final DeferredRegister<MapDecorationType> MAP_DECORATION_TYPES =
        DeferredRegister.create(BuiltInRegistries.MAP_DECORATION_TYPE, NoMansLand.MODID);

    public static final DeferredHolder<MapDecorationType, MapDecorationType> MOONLIGHT_BASIN =
        MAP_DECORATION_TYPES.register("moonlight_basin", () -> new MapDecorationType(
            NoMansLand.location("moonlight_basin"), true, -1, false, false
        ));

    public static final DeferredHolder<MapDecorationType, MapDecorationType> BELL_SANCTUARY =
        MAP_DECORATION_TYPES.register("bell_sanctuary", () -> new MapDecorationType(
            NoMansLand.location("bell_sanctuary"), true, -1, true, false
        ));

    public static final DeferredHolder<MapDecorationType, MapDecorationType> ALCHEMIST_RUINS =
        MAP_DECORATION_TYPES.register("alchemist_ruins", () -> new MapDecorationType(
            NoMansLand.location("alchemist_ruins"), true, -1, true, false
        ));

    public static final DeferredHolder<MapDecorationType, MapDecorationType> MINESHAFT =
        MAP_DECORATION_TYPES.register("mineshaft", () -> new MapDecorationType(
            NoMansLand.location("mineshaft"), true, -1, true, false
        ));
}
