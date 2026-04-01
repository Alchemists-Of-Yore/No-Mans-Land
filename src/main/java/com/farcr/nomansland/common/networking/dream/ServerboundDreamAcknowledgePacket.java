package com.farcr.nomansland.common.networking.dream;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import com.farcr.nomansland.common.registry.NMLRegistries;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ServerboundDreamAcknowledgePacket() implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ServerboundDreamAcknowledgePacket> STREAM_CODEC = StreamCodec.unit(new ServerboundDreamAcknowledgePacket());

    public static final Type<ServerboundDreamAcknowledgePacket> TYPE = new Type<>(NoMansLand.location("client/dream_ack"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleData(IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> {
                NoMansLand.LOGGER.info("dirty client removal");
                DreamLevelHandler.dirtyClients.remove((ServerPlayer) context.player());
            });
        }
    }
}
