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
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DreamingPlayerRenderer extends EntityRenderer<DreamingPlayer> {
    public DreamingPlayerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(DreamingPlayer entity) { return null; }

    public Player getRemotePlayer(DreamingPlayer player) {
        if (player.clientOnlyRemotePlayer == null) {
            assert Minecraft.getInstance().player != null;
            ClientPacketListener clientpacketlistener = Minecraft.getInstance().player.connection;
            if (player.getClientSnapshot() == null)
                return null;

            PlayerInfo playerinfo = clientpacketlistener.getPlayerInfo(player.getClientSnapshot().uuid());
            if (playerinfo == null)
                return null;
            player.clientOnlyRemotePlayer = new RemotePlayer(
                (ClientLevel) player.level(),
                playerinfo.getProfile()
            );
            player.clientOnlyRemotePlayer.setPose(Pose.SLEEPING);
        }
        return player.clientOnlyRemotePlayer;
    }

    @Override
    public void render(@NotNull DreamingPlayer p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        RemotePlayer remotePlayer = (RemotePlayer) getRemotePlayer(p_entity);
        if (remotePlayer == null)
            return;

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
