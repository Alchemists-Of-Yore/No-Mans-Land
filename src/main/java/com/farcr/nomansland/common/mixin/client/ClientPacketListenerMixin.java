package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.friend.dream.DreamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    /* Dreams should just trust the client lol */
    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void nml$suppressMovementPacket(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (DreamManager.Client.getInstance().dreamShouldRender())
            ci.cancel();
    }
}
