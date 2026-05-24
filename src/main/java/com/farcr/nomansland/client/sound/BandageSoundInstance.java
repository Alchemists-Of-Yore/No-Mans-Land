package com.farcr.nomansland.client.sound;

import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class BandageSoundInstance extends AbstractTickableSoundInstance {
    private static final Map<Integer, BandageSoundInstance> ACTIVE = new HashMap<>();

    private final Player player;

    public static void play(Player player) {
        stopFor(player.getId());
        BandageSoundInstance instance = new BandageSoundInstance(player);
        ACTIVE.put(player.getId(), instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    public static void stopFor(int playerId) {
        BandageSoundInstance existing = ACTIVE.remove(playerId);
        if (existing != null) Minecraft.getInstance().getSoundManager().stop(existing);
    }

    public BandageSoundInstance(Player player) {
        super(NMLSounds.BANDAGE_WRAP.get(), SoundSource.PLAYERS, RandomSource.create());
        this.player = player;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.volume = 0.8F;
        this.pitch = 1;
    }

    @Override
    public void tick() {
        if (!player.isAlive() || player.isRemoved()) {
            ACTIVE.remove(player.getId(), this);
            this.stop();
            return;
        }
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }
}
