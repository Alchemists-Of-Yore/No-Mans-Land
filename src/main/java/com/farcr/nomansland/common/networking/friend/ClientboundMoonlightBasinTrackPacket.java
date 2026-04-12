package com.farcr.nomansland.common.networking.friend;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundMoonlightBasinTrackPacket(
    BlockPos pos, boolean hasGrantedFriendship
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundMoonlightBasinTrackPacket> STREAM_CODEC  = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ClientboundMoonlightBasinTrackPacket::pos,
        ByteBufCodecs.BOOL,
        ClientboundMoonlightBasinTrackPacket::hasGrantedFriendship,
        ClientboundMoonlightBasinTrackPacket::new
    );
    public static final Type<ClientboundMoonlightBasinTrackPacket> TYPE = new Type<>(NoMansLand.location("client/friend_moon/moon_basin_update"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                FriendMoonRenderer.getInstance().setClientBlockPos(pos, hasGrantedFriendship);
            });
        }
    }
}