package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.FriendMoonUpdate;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record ServerboundFriendMoonUpdatePacket(
    FriendMoonUpdate packetType
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ServerboundFriendMoonUpdatePacket> STREAM_CODEC  = StreamCodec.composite(
        FriendMoonUpdate.STREAM_CODEC,
        ServerboundFriendMoonUpdatePacket::packetType,
        ServerboundFriendMoonUpdatePacket::new
    );
    public static final CustomPacketPayload.Type<ServerboundFriendMoonUpdatePacket> TYPE = new CustomPacketPayload.Type<>(NoMansLand.location("server/moon_awake"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> {
                FriendMoon friendMoon = FriendMoon.getOrDefault(context.player().getServer().overworld());
                if (context.player().hasEffect(FriendMoon.FRIENDSHIP))
                    friendMoon.packetUpdateEvent(packetType());
            });
        }
    }
}
