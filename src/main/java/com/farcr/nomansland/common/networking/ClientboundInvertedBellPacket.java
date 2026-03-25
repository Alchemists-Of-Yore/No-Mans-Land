package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public enum ClientboundInvertedBellPacket implements CustomPacketPayload {
    FADE_IN, FADE_OUT, FADE_OUT_PAINFUL;

    public static final StreamCodec<ByteBuf, ClientboundInvertedBellPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, p -> (byte) p.ordinal(), (e) -> ClientboundInvertedBellPacket.values()[e]
    );

    public static final Type<ClientboundInvertedBellPacket> TYPE = new Type<>(NoMansLand.location("client/inverted_bell"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                switch (this) {
                    case FADE_IN -> InvertedBellClientHandler.instance.startFadeIn();
                    case FADE_OUT ->  InvertedBellClientHandler.instance.startFadeOut();
                    case FADE_OUT_PAINFUL -> InvertedBellClientHandler.instance.startFadeOutPainful();
                }
            });
        }
    }
}
