package com.farcr.nomansland.common.friend.dialogue;

import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueTrackerPacket;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DialogueTracker extends SavedData {
    public static final String NAME = "dialogue_tracker";

    public static DialogueTracker getOrDefault(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            DialogueTracker::new,
            (tag, provider) -> new DialogueTracker().load(tag)
        ), NAME);
    }

    public static class PlayerDialogueData {
        public boolean heardAnyDialogue = false;
        public final Set<ResourceLocation> heardOfferingDialogues = new HashSet<>();
    }

    private final Map<UUID, PlayerDialogueData> playerData = new HashMap<>();

    public PlayerDialogueData getData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, (uuid) -> new PlayerDialogueData());
    }

    public void markDialogueHeard(ServerPlayer player, ResourceLocation registryLocation, ResourceLocation dialogueLocation) {
        PlayerDialogueData data = getData(player.getUUID());
        boolean changed = !data.heardAnyDialogue;
        data.heardAnyDialogue = true;
        if (NMLRegistries.OFFERING_DIALOGUE_KEY.location().equals(registryLocation) && dialogueLocation != null)
            changed |= data.heardOfferingDialogues.add(dialogueLocation);
        if (changed) {
            setDirty();
            sync(player);
        }
    }

    public void sync(ServerPlayer player) {
        PlayerDialogueData data = getData(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ClientboundDialogueTrackerPacket(
            data.heardAnyDialogue, new ArrayList<>(data.heardOfferingDialogues)
        ));
    }

    public DialogueTracker load(CompoundTag tag) {
        playerData.clear();
        for (Tag entryTag : tag.getList("Players", Tag.TAG_COMPOUND)) {
            if (!(entryTag instanceof CompoundTag playerTag) || !playerTag.contains("Player"))
                continue;
            PlayerDialogueData data = new PlayerDialogueData();
            data.heardAnyDialogue = playerTag.getBoolean("HeardAnyDialogue");
            for (Tag heardTag : playerTag.getList("HeardDialogues", Tag.TAG_STRING)) {
                ResourceLocation location = ResourceLocation.tryParse(heardTag.getAsString());
                if (location != null)
                    data.heardOfferingDialogues.add(location);
            }
            playerData.put(NbtUtils.loadUUID(playerTag.get("Player")), data);
        }
        return this;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag playersTag = new ListTag();
        playerData.forEach((uuid, data) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.put("Player", NbtUtils.createUUID(uuid));
            playerTag.putBoolean("HeardAnyDialogue", data.heardAnyDialogue);
            ListTag heardTag = new ListTag();
            data.heardOfferingDialogues.forEach((location) -> heardTag.add(StringTag.valueOf(location.toString())));
            playerTag.put("HeardDialogues", heardTag);
            playersTag.add(playerTag);
        });
        tag.put("Players", playersTag);
        return tag;
    }
}
