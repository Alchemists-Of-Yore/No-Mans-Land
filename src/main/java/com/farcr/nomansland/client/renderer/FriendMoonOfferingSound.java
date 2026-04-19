package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.friend.FriendMoonState;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FriendMoonOfferingSound extends AbstractTickableSoundInstance {
    public static final float TARGET_VOLUME = 0.3f;
    public static final int FADE_TICKS = 20;
    public static final float FADE_RATE = 1f / FADE_TICKS;

    private final BlockPos basinPos;
    private boolean fadingOut = false;

    public FriendMoonOfferingSound(BlockPos basinPos) {
        super(NMLSounds.FRIEND_MOON_OFFERING_LOOP.get(), SoundSource.AMBIENT, RandomSource.create());
        this.basinPos = basinPos;
        this.looping = true;
        this.delay = 0;
        this.volume = 0f;
        this.pitch = 1.0f;
        this.x = basinPos.getX() + 0.5;
        this.y = basinPos.getY() + 0.5;
        this.z = basinPos.getZ() + 0.5;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (Minecraft.getInstance().level == null) {
            this.stop();
            return;
        }
        BlockEntity be = Minecraft.getInstance().level.getBlockEntity(basinPos);
        boolean shouldPlay = be instanceof MoonlightBasinBlockEntity basin
            && basin.clientMoon != null
            && basin.clientMoon.getState() == FriendMoonState.OFFERING
            && basin.clientMoon.isActive();
        if (!shouldPlay) fadingOut = true;

        if (fadingOut) {
            this.volume = Mth.lerp(FADE_RATE, this.volume, 0f);
            if (this.volume < 0.01f) this.stop();
        } else if (this.volume < TARGET_VOLUME) {
            this.volume = Mth.lerp(FADE_RATE, this.volume, TARGET_VOLUME);
        }
    }
}
