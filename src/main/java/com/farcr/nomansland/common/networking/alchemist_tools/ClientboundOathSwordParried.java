package com.farcr.nomansland.common.networking.alchemist_tools;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.extensions.AncestralOathSwordClientExtensions;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.items.NMLItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundOathSwordParried(
    int playerId
) implements CustomPacketPayload {
    public static final Type<ClientboundOathSwordParried> TYPE = new Type<>(NoMansLand.location("client/ancestral_oath_sword/parry_success"));
    public static final StreamCodec<ByteBuf, ClientboundOathSwordParried> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, ClientboundOathSwordParried::playerId,
        ClientboundOathSwordParried::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}

    public void handleData(IPayloadContext context) {
        context.enqueueWork( () -> {
            // CURRENTLY only first person / for player in client for testing purposes
            if (context.player().getId() == playerId) {
                ((AncestralOathSwordClientExtensions) IClientItemExtensions.of(NMLItems.ANCESTRAL_OATH_SWORD.item()))
                    .parrySuccessful();
            }
        });
    }
}
