package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.worldevent.SunDog;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundSunDogStatePacket(ResourceKey<Level> dimension, boolean state, boolean fromLevelJoin) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundSunDogStatePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION),
            ClientboundSunDogStatePacket::dimension,
            ByteBufCodecs.BOOL,
            ClientboundSunDogStatePacket::state,
            ByteBufCodecs.BOOL,
            ClientboundSunDogStatePacket::fromLevelJoin,
            ClientboundSunDogStatePacket::new
    );

    public static final Type<ClientboundSunDogStatePacket> TYPE = new Type<>(NoMansLand.location("client/sun_dog"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Level level = player.level();
                if (level.dimension() != dimension()) return;
                SunDog.Client.INSTANCE.informOfState(state(), fromLevelJoin());
            });
        }
    }
}