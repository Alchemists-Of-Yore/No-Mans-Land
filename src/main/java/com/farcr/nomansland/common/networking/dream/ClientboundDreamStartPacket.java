package com.farcr.nomansland.common.networking.dream;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.dream.DreamManager;
import com.farcr.nomansland.common.registry.NMLRegistries;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ClientboundDreamStartPacket(
    ResourceLocation dreamType
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDreamStartPacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        ClientboundDreamStartPacket::dreamType,
        ClientboundDreamStartPacket::new
    );

    public static final Type<ClientboundDreamStartPacket> TYPE = new Type<>(NoMansLand.location("client/dream"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleData(IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                DreamManager.Client clientRenderer = DreamManager.Client.getInstance();
                clientRenderer.clientSetDream(
                    NMLRegistries.DREAM_TYPE.get(dreamType)
                );
            });
        }
    }
}
