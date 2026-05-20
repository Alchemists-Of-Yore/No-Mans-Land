package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ClientboundRitualPickResponsePacket(List<BlockPos> positions) implements CustomPacketPayload {
    public static final Type<ClientboundRitualPickResponsePacket> TYPE = new Type<>(NoMansLand.location("client/ritual_pick/response"));
    public static final StreamCodec<ByteBuf, ClientboundRitualPickResponsePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundRitualPickResponsePacket::positions,
            ClientboundRitualPickResponsePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        context.enqueueWork( () -> {
            // debug: just spawn some particles lawl
            ClientLevel level = Minecraft.getInstance().level;
            for (BlockPos position : positions) {
                for (int i = 0; i < 4; i++) {
                    double x = position.getX() + level.random.nextDouble(),
                           y = position.getY() + level.random.nextDouble(),
                           z = position.getZ() + level.random.nextDouble();

                    // freakin' sweet
                    level.addAlwaysVisibleParticle(
                            NMLParticleTypes.FUNNY_PLACEHOLDER_DEBUG_RITUAL_PICK.get(),
                                x, y, z, 0,0,0
                    );
                }
            }
        });
    }
}
