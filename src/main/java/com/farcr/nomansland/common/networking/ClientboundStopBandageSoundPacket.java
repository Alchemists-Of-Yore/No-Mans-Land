package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundStopBandageSoundPacket(int playerId) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundStopBandageSoundPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ClientboundStopBandageSoundPacket::playerId,
        ClientboundStopBandageSoundPacket::new
    );

    public static final Type<ClientboundStopBandageSoundPacket> TYPE = new Type<>(NoMansLand.location("client/bandage_sound_stop"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> com.farcr.nomansland.client.sound.BandageSoundInstance.stopFor(playerId()));
        }
    }
}
