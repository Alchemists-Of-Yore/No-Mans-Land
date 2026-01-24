package com.farcr.nomansland.common.registry.items;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

public class NMLDiscs {
    public static final ResourceKey<JukeboxSong> GUIDANCE = ResourceKey.create(Registries.JUKEBOX_SONG, NoMansLand.location("guidance"));

}