package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundBuddyCrouchPacket(
    int buddyID
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundBuddyCrouchPacket> STREAM_CODEC  = StreamCodec.composite(
        ByteBufCodecs.INT, ClientboundBuddyCrouchPacket::buddyID,
        ClientboundBuddyCrouchPacket::new
    );
    public static final CustomPacketPayload.Type<ClientboundBuddyCrouchPacket> TYPE = new CustomPacketPayload.Type<>(NoMansLand.location("client/buddy_crouch"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Entity entity = player.level().getEntity(buddyID);
                if (entity instanceof Buddy buddy)
                    buddy.crouch();
            });
        }
    }
}
