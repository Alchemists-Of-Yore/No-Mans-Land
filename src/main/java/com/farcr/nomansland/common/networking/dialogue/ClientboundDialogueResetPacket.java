package com.farcr.nomansland.common.networking.dialogue;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.DialogueRenderer;
import com.farcr.nomansland.common.friend.dialogue.DialogueState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundDialogueResetPacket() implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDialogueResetPacket> STREAM_CODEC = StreamCodec.unit(new ClientboundDialogueResetPacket());

    public static final Type<ClientboundDialogueResetPacket> TYPE = new Type<>(NoMansLand.location("client/dialogue/reset"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                DialogueState state = DialogueRenderer.getCurrentState();
                if (state != null)
                    state.reset();
                DialogueRenderer.setCurrentState(state);
            });
        }
    }
}