package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.extension.ClientChunkCacheExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// todo: inverted bell seamless teleport black magic
public record ClientboundDistantChunkPacket(int x, int z) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ClientboundDistantChunkPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClientboundDistantChunkPacket::x,
            ByteBufCodecs.INT,
            ClientboundDistantChunkPacket::z,
            ClientboundDistantChunkPacket::new
    );

    public static final Type<ClientboundDistantChunkPacket> TYPE = new Type<>(NoMansLand.location("client/distant_chunk"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                ((ClientChunkCacheExtension)Minecraft.getInstance().level.getChunkSource()).nml$addToOverride(this.x, this.z);
            });
        }
    }
}
