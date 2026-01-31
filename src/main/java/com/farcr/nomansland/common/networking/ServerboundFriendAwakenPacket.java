package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.moonlight.MoonlightBasinBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundFriendAwakenPacket() implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ServerboundFriendAwakenPacket> STREAM_CODEC = StreamCodec.unit(new ServerboundFriendAwakenPacket());
    public static final CustomPacketPayload.Type<ServerboundFriendAwakenPacket> TYPE = new CustomPacketPayload.Type<>(NoMansLand.location("server/moon_awake"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        context.enqueueWork(MoonlightBasinBlockEntity::wakeUpMoon);
    }
}
