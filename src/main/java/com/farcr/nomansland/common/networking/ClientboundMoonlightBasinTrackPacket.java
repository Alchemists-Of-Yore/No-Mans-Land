package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundMoonlightBasinTrackPacket(
    BlockPos pos
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundMoonlightBasinTrackPacket> STREAM_CODEC  = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ClientboundMoonlightBasinTrackPacket::pos,
        ClientboundMoonlightBasinTrackPacket::new
    );
    public static final Type<ClientboundMoonlightBasinTrackPacket> TYPE = new Type<>(NoMansLand.location("client/moon_basin_update"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        context.enqueueWork(() -> {
            FriendMoonRenderer.clientBlockPos = pos();
        });
    }
}
