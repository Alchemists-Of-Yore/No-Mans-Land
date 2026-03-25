package com.farcr.nomansland.common.networking.dream;

import com.farcr.nomansland.NoMansLand;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerboundDreamEndPacket() implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ServerboundDreamEndPacket> STREAM_CODEC = StreamCodec.unit(new ServerboundDreamEndPacket());

    public static final Type<ServerboundDreamEndPacket> TYPE = new Type<>(NoMansLand.location("server/dream"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
