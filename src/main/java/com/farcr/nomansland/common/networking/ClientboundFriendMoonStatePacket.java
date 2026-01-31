package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.block.moonlight.MoonlightBasinBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundFriendMoonStatePacket(
    FriendMoonRenderer.FriendMoonAnimation state
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundFriendMoonStatePacket> STREAM_CODEC  = StreamCodec.composite(
        FriendMoonRenderer.FriendMoonAnimation.STREAM_CODEC,
        ClientboundFriendMoonStatePacket::state,
        ClientboundFriendMoonStatePacket::new
    );
    public static final Type<ClientboundFriendMoonStatePacket> TYPE = new Type<>(NoMansLand.location("client/moon_animation"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        context.enqueueWork(() -> {
            FriendMoonRenderer.setFriendMoonState(this.state());
        });
    }
}
