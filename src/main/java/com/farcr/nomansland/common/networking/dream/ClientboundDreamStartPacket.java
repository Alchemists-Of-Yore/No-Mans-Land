package com.farcr.nomansland.common.networking.dream;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import com.farcr.nomansland.common.registry.NMLRegistries;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

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
                ClientDreamRenderer clientRenderer = ClientDreamRenderer.getInstance();
                clientRenderer.clientSetDream(
                    NMLRegistries.DREAM_TYPE.get(dreamType));
                ;
                Set<ResourceKey<Level>> dimensionList = Minecraft.getInstance().player.connection.levels();

                ResourceKey<Level> levelKey = DreamLevelHandler.resourceKey(
                    Registries.DIMENSION, dreamType, context.player());
                dimensionList.add(levelKey);

                DreamLevelHandler.registerDimensionType(
                    context.player().registryAccess(),
                    NMLRegistries.DREAM_TYPE.get(dreamType),
                    context.player()
                );
            });
        }
    }
}
