package com.farcr.nomansland.common.networking.dialogue;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.DialogueRenderer;
import com.farcr.nomansland.common.friend.FriendMoon;
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
        Optional<UUID> playerUUID,
        Optional<Boolean> timed
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDialoguePacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        ClientboundDialoguePacket::resourceLocation,
        ResourceLocation.STREAM_CODEC,
        ClientboundDialoguePacket::registryLocation,
        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
        ClientboundDialoguePacket::playerUUID,
        ByteBufCodecs.optional(ByteBufCodecs.BOOL),
        ClientboundDialoguePacket::timed,
        ClientboundDialoguePacket::new
    );

    public static ClientboundDialoguePacket newDialoguePacket(
        ResourceLocation resourceLocation, ResourceLocation registryLocation, Optional<UUID> playerUUID
    ) {
        return new ClientboundDialoguePacket(resourceLocation, registryLocation, playerUUID, Optional.empty());
    }

    public static ClientboundDialoguePacket timedDialoguePacket(
        ResourceLocation resourceLocation, ResourceLocation registryLocation, Optional<UUID> playerUUID
    ) {
        return new ClientboundDialoguePacket(resourceLocation, registryLocation, playerUUID, Optional.of(true));
    }

    public static final CustomPacketPayload.Type<ClientboundDialoguePacket> TYPE = new CustomPacketPayload.Type<>(NoMansLand.location("client/dialogue/update"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void applyPacket(Level level, Player player) {
        ResourceKey<Registry<DialogueRegistry.DialoguePool>> tempKey = ResourceKey.createRegistryKey(registryLocation);
        Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, tempKey);
        DialogueRegistry.DialoguePool dialoguePool = dialogueRegistry.get(resourceLocation);

        // Set Dialogue
        assert dialoguePool != null;
        DialogueRenderer.setCurrentState(new DialogueState(
            resourceLocation, tempKey.location().getPath().replace("/", "."), dialoguePool
        ));
        if (playerUUID.isPresent()) {
            Player targetPlayer = level.getPlayerByUUID(playerUUID.get());
            if (targetPlayer != null) {
                DialogueRenderer.getCurrentState().translateDialogue.setPlayerName(
                    targetPlayer.getName().getString()
                );
            }
        }
        timed.ifPresent((tickAmount) -> DialogueRenderer.getCurrentState().setTicks(
            FriendMoon.calculateDialogueTicks(DialogueRenderer.getCurrentState().originalDialogue.getTextLength(), player.getRandom())));
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Level level = player.level();
                applyPacket(level, player);
            });
        }
    }
}