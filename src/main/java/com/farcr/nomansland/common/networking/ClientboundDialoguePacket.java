package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dialogue.DialogueState;
import com.farcr.nomansland.common.dialogue.DialogueUtil;
import com.farcr.nomansland.client.renderer.DialogueRenderer;
import com.farcr.nomansland.common.dialogue.DialogueRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundDialoguePacket(ResourceLocation resourceLocation, ResourceLocation registryLocation) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDialoguePacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        ClientboundDialoguePacket::resourceLocation,
        ResourceLocation.STREAM_CODEC,
        ClientboundDialoguePacket::registryLocation,
        ClientboundDialoguePacket::new
    );

    public static final CustomPacketPayload.Type<ClientboundDialoguePacket> TYPE = new CustomPacketPayload.Type<>(NoMansLand.location("client/dialogue"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ResourceKey<Registry<DialogueRegistry.DialoguePool>> tempKey = ResourceKey.createRegistryKey(registryLocation);
            Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(player.level(), tempKey);
            DialogueRegistry.DialoguePool dialoguePool = dialogueRegistry.get(resourceLocation);

            // Set Dialogue
            assert dialoguePool != null;
            DialogueRenderer.setCurrentState(new DialogueState(
                resourceLocation, dialoguePool
            ));
        });
    }
}