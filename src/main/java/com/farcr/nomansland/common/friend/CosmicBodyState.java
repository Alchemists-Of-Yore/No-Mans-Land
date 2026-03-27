package com.farcr.nomansland.common.friend;

import com.farcr.nomansland.common.dreams.dreamlevel.DreamPlayerSnapshot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CosmicBodyState(
    BlockPos playerPosition,
    int daysCounted
) {
    public static final Codec<CosmicBodyState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BlockPos.CODEC.fieldOf("BlockPos").forGetter(CosmicBodyState::playerPosition),
            Codec.INT.fieldOf("Time").forGetter(CosmicBodyState::daysCounted)
        ).apply(instance, CosmicBodyState::new)
    );

    private static final int MAX_COSMIC_BODY_DAYS = 8;
    public boolean exceedsDays() {
        return (daysCounted() > (MAX_COSMIC_BODY_DAYS - 1));
    }
}
