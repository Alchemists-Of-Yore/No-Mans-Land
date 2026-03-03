package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.DialogueRenderer;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.friend.dialogue.DialogueState;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.UUID;

public record ClientboundDialoguePacket(
        ResourceLocation resourceLocation,
        ResourceLocation registryLocation,
        Optional<UUID> playerUUID
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDialoguePacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        ClientboundDialoguePacket::resourceLocation,
        ResourceLocation.STREAM_CODEC,
        ClientboundDialoguePacket::registryLocation,
        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
        ClientboundDialoguePacket::playerUUID,
        ClientboundDialoguePacket::new
    );

    public static final CustomPacketPayload.Type<ClientboundDialoguePacket> TYPE = new CustomPacketPayload.Type<>(NoMansLand.location("client/friend_moon/dialogue"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Level level = player.level();
                ResourceKey<Registry<DialogueRegistry.DialoguePool>> tempKey = ResourceKey.createRegistryKey(registryLocation);
                Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, tempKey);
                DialogueRegistry.DialoguePool dialoguePool = dialogueRegistry.get(resourceLocation);

                // Set Dialogue
                assert dialoguePool != null;
                DialogueRenderer.setCurrentState(new DialogueState(
                    resourceLocation, dialoguePool
                ));
                if (playerUUID.isPresent()) {
                    Player targetPlayer = level.getPlayerByUUID(playerUUID.get());
                    DialogueRenderer.getCurrentState().translateDialogue.setPlayerName(
                        targetPlayer.getName().getString()
                    );
                }
            });
        }
    }
}