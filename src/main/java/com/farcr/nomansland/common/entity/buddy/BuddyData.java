package com.farcr.nomansland.common.entity.buddy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Optional;

public class BuddyData {
    public BuddyData(int moonCycle) {
        this.moonCycle = moonCycle;
    }

    public static BuddyData fromCodec(int moonCycle, boolean shouldRespawn, Optional<CompoundTag> tag) {
        BuddyData buddyData = new BuddyData(moonCycle);
        if (shouldRespawn) {
            buddyData.queryRespawn();
            tag.ifPresent(buddyData::setNBTData);
        }
        return buddyData;
    }

    public static final Codec<BuddyData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("moonCycle").forGetter(BuddyData::getMoonCycle),
            Codec.BOOL.fieldOf("shouldRespawn").forGetter(BuddyData::getShouldRespawn),
            CompoundTag.CODEC.optionalFieldOf("buddyData").forGetter(BuddyData::getNBTData)
        ).apply(instance, BuddyData::fromCodec)
    );

    private final int moonCycle;
    public int getMoonCycle() {
        return moonCycle;
    }

    private boolean shouldRespawn = false;
    private boolean getShouldRespawn() {
        return shouldRespawn;
    }
    public boolean tryRespawning() { return getShouldRespawn(); }

    private @Nullable CompoundTag nbtData;
    public Optional<CompoundTag> getNBTData() {
        return Optional.ofNullable(nbtData);
    }

    public void setNBTData(CompoundTag tag) {
        this.nbtData = tag.copy();
    }

    public void queryRespawn() {
        shouldRespawn = true;
    }
}
