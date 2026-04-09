package com.farcr.nomansland.client.ambience.fogmodifiers;

import com.farcr.nomansland.client.ambience.FogModifierHandler;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;

public class FriendMoonFogModifier extends FogModifier {
    @Override
    public float getFogEndAddend() {
        return -.25f;
    }

    public float opacity() {
        return FriendMoonRenderer.getInstance().getFriendMoonOpacity();
    }

    @Override
    public float getFogStartAddend() {
        return -1F;
    }

    static float redModifier = (116 / 255f);
    static float greenModifier = (119 / 255f);
    static float blueModifier = (78 / 255f);

    @Override
    public float getFogRedMultiplier() { return redModifier * opacity(); }

    @Override
    public float getFogGreenMultiplier() { return greenModifier * opacity(); }

    @Override
    public float getFogBlueMultiplier() { return (0.25f * (1f - opacity())) + (blueModifier * opacity()); }

    @Override
    boolean active(FogModifierHandler.FogContext context) {
        return Minecraft.getInstance().player != null && opacity() > 0.01f;
    }
}
