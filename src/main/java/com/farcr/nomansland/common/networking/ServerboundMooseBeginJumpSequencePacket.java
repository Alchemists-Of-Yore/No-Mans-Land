package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ServerboundMooseBeginJumpSequencePacket(UUID mooseID) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ServerboundMooseBeginJumpSequencePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundMooseBeginJumpSequencePacket::mooseID,
            ServerboundMooseBeginJumpSequencePacket::new
    );

    public static final Type<ServerboundMooseBeginJumpSequencePacket> TYPE = new Type<>(NoMansLand.location("server/moose_jump"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        Player player = context.player();
        if (player.level() instanceof ServerLevel level) {
            if (level.getEntity(mooseID) instanceof Moose moose) {
                moose.onPlayerStartChargingJump();
            }
        }
    }
}