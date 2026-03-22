package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamPlayerSnapshot;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamingPlayer;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class DreamingPlayerRenderer extends EntityRenderer<DreamingPlayer> {
    public DreamingPlayerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(DreamingPlayer entity) { return null; }

    private RemotePlayer remotePlayer;

    @Override
    public void render(DreamingPlayer p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        DreamPlayerSnapshot snapshot = p_entity.getClientSnapshot();
        if (snapshot == null)
            return;

        if (remotePlayer == null) {
            ClientPacketListener clientpacketlistener = Minecraft.getInstance().player.connection;
            PlayerInfo playerinfo = clientpacketlistener.getPlayerInfo(snapshot.uuid());
            if (playerinfo == null)
                return;

            remotePlayer = new RemotePlayer(
                (ClientLevel) p_entity.level(),
                playerinfo.getProfile()
            );
            remotePlayer.setPose(Pose.SLEEPING);
        }

        remotePlayer.setXRot(p_entity.getXRot());
        remotePlayer.setYRot(p_entity.getYRot());
        remotePlayer.setYBodyRot(p_entity.yBodyRot);
        remotePlayer.setYHeadRot(p_entity.yHeadRot);
        p_entity.getSleepingPos().ifPresent(remotePlayer::setSleepingPos);

        dispatcher.render(
            remotePlayer, 0, 0, 0, entityYaw,
            0f, poseStack, bufferSource, packedLight
        );
    }
}
