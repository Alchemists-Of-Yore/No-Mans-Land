package com.farcr.nomansland.common.dreams.dreamlevel;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

// in case I need to provide more information than just the UUID
public record DreamPlayerSnapshot(
    UUID uuid, CompoundTag compoundTag,
    Byte playerModelCustomization
) {
    public static final StreamCodec<FriendlyByteBuf, DreamPlayerSnapshot> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        DreamPlayerSnapshot::uuid,
        ByteBufCodecs.COMPOUND_TAG,
        DreamPlayerSnapshot::compoundTag,
        ByteBufCodecs.BYTE,
        DreamPlayerSnapshot::playerModelCustomization,
        DreamPlayerSnapshot::new
    );
}
