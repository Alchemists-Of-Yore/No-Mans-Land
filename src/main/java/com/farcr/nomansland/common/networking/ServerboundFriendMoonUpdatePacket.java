package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record ServerboundFriendMoonUpdatePacket(
    BlockPos pos,
    MoonlightBasinBlockEntity.FriendMoonUpdatePacket packetType
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ServerboundFriendMoonUpdatePacket> STREAM_CODEC  = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ServerboundFriendMoonUpdatePacket::pos,
        MoonlightBasinBlockEntity.FriendMoonUpdatePacket.STREAM_CODEC,
        ServerboundFriendMoonUpdatePacket::packetType,
        ServerboundFriendMoonUpdatePacket::new
    );
    public static final CustomPacketPayload.Type<ServerboundFriendMoonUpdatePacket> TYPE = new CustomPacketPayload.Type<>(NoMansLand.location("server/moon_awake"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        context.enqueueWork(() -> {
            Optional<MoonlightBasinBlockEntity> optionalBasin = context.player().level().getBlockEntity(pos(), NMLBlockEntities.MOONLIGHT_BASIN.get());
            if (optionalBasin.isPresent()) {
                MoonlightBasinBlockEntity basinEntity = optionalBasin.get();
                basinEntity.packetUpdateEvent(packetType());
            }
        });
    }
}
