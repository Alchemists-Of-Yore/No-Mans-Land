package com.farcr.nomansland.common.networking.dialogue;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.ClientDialogueTracker;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ClientboundDialogueTrackerPacket(
    boolean heardAnyDialogue,
    List<ResourceLocation> heardOfferingDialogues
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDialogueTrackerPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        ClientboundDialogueTrackerPacket::heardAnyDialogue,
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
        ClientboundDialogueTrackerPacket::heardOfferingDialogues,
        ClientboundDialogueTrackerPacket::new
    );

    public static final Type<ClientboundDialogueTrackerPacket> TYPE = new Type<>(NoMansLand.location("client/dialogue/tracker"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> ClientDialogueTracker.acceptSync(heardAnyDialogue, heardOfferingDialogues));
        }
    }
}
