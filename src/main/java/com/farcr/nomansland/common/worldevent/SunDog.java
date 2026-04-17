package com.farcr.nomansland.common.worldevent;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.networking.ClientboundSunDogStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;

// TODO: make a generalized system for "world events"
public class SunDog extends SavedData {
    public static SunDog getOrDefault(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                () -> new SunDog(level),
                (tag, provider) -> new SunDog(level, tag, provider)
        ), "sun_dog");
    }
    public static ResourceLocation SUN_DOG_MORNING_LOCATION = NoMansLand.location("sun_dog/morning"),
                                   SUN_DOG_STORM_END_LOCATION = NoMansLand.location("sun_dog/storm_end");

    ServerLevel level;
    RandomSource morningRandom, stormEndRandom;
    boolean active;
    long startTime, endTime;

    public SunDog(ServerLevel level) {
        this.level = level;
        this.morningRandom = level.getRandomSequence(SUN_DOG_MORNING_LOCATION);
        this.stormEndRandom = level.getRandomSequence(SUN_DOG_STORM_END_LOCATION);

        this.active = false;
        this.startTime = -1;
        this.endTime = -1;
    }

    public SunDog(ServerLevel level, CompoundTag tag, HolderLookup.Provider lookupProvider) {
        this(level);
        this.active = tag.getBoolean("Active");
        this.startTime = tag.getLong("StartTime");
        this.endTime = tag.getLong("EndTime");
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("Active", this.active);
        tag.putLong("StartTime", this.startTime);
        tag.putLong("EndTime", this.endTime);

        return tag;
    }

    public void tick() {
        if (!active) return;
        if (level == null) return;
        if (level.getGameTime() >= endTime) end();
    }

    boolean attemptStart(RandomSource random) {
        long currentTime = level.getGameTime();
        boolean isAlreadyStarted = active;
        // don't start if the last sun dog occurrence was less than 5 minutes ago
        if (!isAlreadyStarted && (currentTime - endTime <= 5 * 60 * 20)) return false;

        active = true;

        // last between 5 and 10 minutes
        int duration = 20 * random.nextIntBetweenInclusive(5 * 60, 10 * 60);
        if (!isAlreadyStarted) startTime = currentTime;
        endTime = currentTime + duration;

        this.setDirty();
        if (!isAlreadyStarted) {
            PacketDistributor.sendToPlayersInDimension(level, new ClientboundSunDogStatePacket(level.dimension(), true, false));
            NoMansLand.LOGGER.info("sun dog initiated!");
        }
        return true;
    }

    public void end() {
        active = false;
        startTime = -1;
        this.setDirty();
        PacketDistributor.sendToPlayersInDimension(level, new ClientboundSunDogStatePacket(level.dimension(), false, false));
    }

    public boolean isActive() {
        return active;
    }

    public void forceStart() {
        long currentTime = level.getGameTime();
        boolean wasActive = active;
        active = true;

        int duration = 20 * level.getRandom().nextIntBetweenInclusive(5 * 60, 10 * 60);
        if (!wasActive) startTime = currentTime;
        endTime = currentTime + duration;

        this.setDirty();
        if (!wasActive) {
            PacketDistributor.sendToPlayersInDimension(level, new ClientboundSunDogStatePacket(level.dimension(), true, false));
            NoMansLand.LOGGER.info("sun dog initiated (forced)!");
        }
    }

    public void maybeStartFromMorning() {
        // 10% chance of starting every morning
        if (morningRandom.nextFloat() <= 0.1)
            attemptStart(morningRandom);
    }

    public void maybeStartFromStormEnd() {
        // 40% chance of starting at the end of a storm
        if (stormEndRandom.nextFloat() <= 0.4)
            attemptStart(stormEndRandom);
    }

    public void informPlayerOfSunDogState(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ClientboundSunDogStatePacket(level.dimension(), this.active, true));
    }

    public static class Client {
        public static final SunDog.Client INSTANCE = new SunDog.Client();

        boolean active = false;
        float transitionFactor, previousTransitionFactor;
        float opacity, previousOpacity;

        public void informOfState(boolean sunDogState, boolean fromLevelJoin) {
            active = sunDogState;
            if (fromLevelJoin) {
                float desiredTransitionFactor = active ? 0 : 1;
                transitionFactor = desiredTransitionFactor;
                previousTransitionFactor = desiredTransitionFactor;
            }
        }

        public void tick() {
            if (Minecraft.getInstance().level == null) return;

            previousOpacity = opacity;
            previousTransitionFactor = transitionFactor;

            if (Minecraft.getInstance().isPaused()) return;

            float desiredTransitionFactor = active ? 1 : 0;
            transitionFactor = Mth.approach(transitionFactor, desiredTransitionFactor, 0.01F);

            BlockPos pos = Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition();
            Biome biome = Minecraft.getInstance().level.getBiome(pos).value();
            float temperature = biome.getTemperature(pos);

            float desiredOpacity = Mth.clampedMap(temperature, -0.3F, 0.2F, 1.0F, 0.0F);
            opacity = Mth.approach(opacity, desiredOpacity, 0.01F);
        }

        public float getOpacity(float partialTick) {
            return Mth.lerp(partialTick, previousOpacity, opacity) * Mth.lerp(partialTick, previousTransitionFactor, transitionFactor);
        }
    }
}
