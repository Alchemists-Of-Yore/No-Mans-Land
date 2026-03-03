package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.moonlight.MoonlightCandleBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/*
* Annoyed with having to make this one a packet but
* i do want the server to track the candles itself for the most part
*/
public record ClientboundCandleLightPacket(BlockPos blockPos) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundCandleLightPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ClientboundCandleLightPacket::blockPos,
        ClientboundCandleLightPacket::new
    );

    public static final CustomPacketPayload.Type<ClientboundCandleLightPacket> TYPE = new CustomPacketPayload.Type<>(NoMansLand.location("client/friend_moon/candle_light"));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                Level level = context.player().level();
                BlockState blockState = level.getBlockState(blockPos());
                if (blockState.getBlock() instanceof MoonlightCandleBlock candleBlock)
                    candleBlock.lightSparkAnimation(blockState, level, blockPos, level.random);
            });
        }
    }
}
