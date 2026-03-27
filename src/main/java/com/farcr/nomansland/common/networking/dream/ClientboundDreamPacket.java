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
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ClientboundDreamPacket(
    Optional<ResourceLocation> optionalDream
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDreamPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        ClientboundDreamPacket::optionalDream,
        ClientboundDreamPacket::new
    );

    public static final Type<ClientboundDreamPacket> TYPE = new Type<>(NoMansLand.location("client/dream"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleData(IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                ClientDreamRenderer clientRenderer = ClientDreamRenderer.getInstance();
                if (optionalDream.isPresent()) {
                    ResourceLocation dreamType = optionalDream.get();
                    clientRenderer.clientSetDream(
                        NMLRegistries.DREAM_TYPE.get(dreamType));

                    assert Minecraft.getInstance().player != null;
                    Set<ResourceKey<Level>> dimensionList = Minecraft.getInstance()
                        .player.connection.levels();
                    ResourceKey<Level> levelKey = DreamLevelHandler.resourceKey(
                        Registries.DIMENSION, dreamType, context.player());
                    dimensionList.add(levelKey);

                    DreamLevelHandler.registerDimensionType(
                        context.player().registryAccess(),
                        NMLRegistries.DREAM_TYPE.get(dreamType),
                        context.player()
                    );
                } else clientRenderer.close();
            });
        }
    }
}
