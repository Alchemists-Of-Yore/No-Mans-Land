package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import net.mehvahdjukaar.moonlight.api.util.codec.EnumStreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public enum InvertedBellPacket implements CustomPacketPayload {
    FADE_IN, FADE_OUT, FADE_OUT_PAINFUL;
    public static final StreamCodec<FriendlyByteBuf, InvertedBellPacket> STREAM_CODEC = new EnumStreamCodec<>(InvertedBellPacket.class);

    public static final Type<InvertedBellPacket> TYPE = new Type<>(NoMansLand.location("client/inverted_bell"));

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
