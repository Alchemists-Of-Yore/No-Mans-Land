package com.farcr.nomansland.common.networking.dialogue;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.condition.DialogueConditionCompiler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundDialogueRegistrySyncPacket() implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDialogueRegistrySyncPacket> STREAM_CODEC = StreamCodec.unit(new ClientboundDialogueRegistrySyncPacket());

    public static final Type<ClientboundDialogueRegistrySyncPacket> TYPE = new Type<>(NoMansLand.location("client/dialogue/sync"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                if (!Minecraft.getInstance().hasSingleplayerServer()) {
                    DialogueConditionCompiler clientCompiler = new DialogueConditionCompiler(context.player().registryAccess());
                    clientCompiler.clearConditionMaps();
                    clientCompiler.compileConditionMaps(null);
                }
            });
        }
    }
}
