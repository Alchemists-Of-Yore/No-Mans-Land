package com.farcr.nomansland.common.networking.friend;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.client.renderer.context.MeetingPointRenderContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ClientboundMeetingPointPacket(
    Optional<BlockPos> lastTrackedPosition,
    Optional<BlockPos> meetingPointPosition
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundMeetingPointPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
        ClientboundMeetingPointPacket::lastTrackedPosition,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
        ClientboundMeetingPointPacket::meetingPointPosition,
        ClientboundMeetingPointPacket::new
    );
    public static final Type<ClientboundMeetingPointPacket> TYPE = new Type<>(NoMansLand.location("client/friend_moon/meeting_point_update"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                boolean shadowIsVisible = lastTrackedPosition.isPresent() && meetingPointPosition.isPresent();
                if (shadowIsVisible) {
                    FriendMoonRenderer.meetingPointContext =
                        new MeetingPointRenderContext(
                            true,
                            lastTrackedPosition.get(),
                            meetingPointPosition.get()
                        );
                    return;
                }
                FriendMoonRenderer.meetingPointContext = MeetingPointRenderContext.fromDefault();
            });
        }
    }
}
