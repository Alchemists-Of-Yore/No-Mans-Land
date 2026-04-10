package com.farcr.nomansland.common.networking.buddy;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundBuddyUpdateEffectsPacket(
    int buddyID, MobEffectInstance effectInstance
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBuddyUpdateEffectsPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        ClientboundBuddyUpdateEffectsPacket::buddyID,
        MobEffectInstance.STREAM_CODEC,
        ClientboundBuddyUpdateEffectsPacket::effectInstance,
        ClientboundBuddyUpdateEffectsPacket::new
    );
    public static final CustomPacketPayload.Type<ClientboundBuddyUpdateEffectsPacket> TYPE =
        new CustomPacketPayload.Type<>(NoMansLand.location("client/buddy_effect"));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleData(IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Entity entity = player.level().getEntity(buddyID);
                if (entity instanceof Buddy buddy) buddy.addEffect(effectInstance);
            });
        }
    }
}
