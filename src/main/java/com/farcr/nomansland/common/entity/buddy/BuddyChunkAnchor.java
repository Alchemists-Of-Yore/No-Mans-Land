package com.farcr.nomansland.common.entity.buddy;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BuddyChunkAnchor extends SavedData {
    private static String NAME = "buddy_anchor";
    // Stores a list of retained values between buddy "respawning"
    public final Map<BlockPos, BuddyData> buddyAnchors = new HashMap<>();

    public static BuddyChunkAnchor getOrDefault(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            () -> new BuddyChunkAnchor(level),
            (tag, provider) -> BuddyChunkAnchor.create(tag, provider, level)
        ), BuddyChunkAnchor.NAME);
    }

    public static BuddyChunkAnchor create(CompoundTag tag, HolderLookup.Provider provider, ServerLevel serverLevel) {
        BuddyChunkAnchor anchor = new BuddyChunkAnchor(serverLevel);
        return anchor.load(tag, provider);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        for (Map.Entry<BlockPos, BuddyData> entry : buddyAnchors.entrySet()) {
            BlockPos pos = entry.getKey();
            BuddyData buddyData = entry.getValue();

            CompoundTag entryTag = new CompoundTag();
            NoMansLand.LOGGER.info("attempting to save : " + pos + " : " + buddyData);
            entryTag.put("Pos", NbtUtils.writeBlockPos(pos));
            entryTag.put("BuddyData", BuddyData.CODEC.encodeStart(NbtOps.INSTANCE, buddyData).getOrThrow());

            listTag.add(entryTag);
        }
        tag.put("BuddySpawnAnchors", listTag);
        return tag;
    }

    public BuddyChunkAnchor load(CompoundTag tag, HolderLookup.Provider provider) {
        buddyAnchors.clear();
        for (Tag entryTag : tag.getList("BuddySpawnAnchors", 10)) {
            if (entryTag instanceof CompoundTag dataTag) {
                BlockPos anchorPosition = NbtUtils.readBlockPos(dataTag, "Pos").orElseThrow();
                BuddyData buddyData = BuddyData.CODEC.parse(NbtOps.INSTANCE, dataTag.get("BuddyData")).getOrThrow();

                buddyAnchors.put(anchorPosition, buddyData);
            }
        }
        return this;
    }

    private final ServerLevel level;
    public BuddyChunkAnchor(ServerLevel level) {
        this.level = level;
    }

    public static final ResourceKey<Structure> FAIRY_RING_KEY = ResourceKey.create(Registries.STRUCTURE, NoMansLand.location("buddy_fairy_ring"));
    public void tickChunk(LevelChunk chunk) {
        if (!chunk.nml$shouldIgnoreBuddyAnchor()) {
            int structuresFound = 0;
            Structure fairyRingStructure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(FAIRY_RING_KEY);
            if (fairyRingStructure != null) {
                StructureManager structureManager = level.structureManager();
                List<StructureStart> structureStarts = structureManager.startsForStructure(chunk.getPos(), structure -> structure.equals(fairyRingStructure));
                for (StructureStart start : structureStarts) {
                    queryBuddyStructure(start);
                    structuresFound++;
                }
                if (structuresFound <= 0)
                    chunk.nml$setIgnoreBuddyAnchor();
            }
        }
    }

    // Returns the cycle, not phase, the moon is currently on.
    public int getCurrentMoonCycle(long dayTime) {
        return (int)((dayTime / 24000L) / 8L);
    }

    public void queryBuddyStructure(StructureStart start) {
        BlockPos spawnBlock = start.getBoundingBox().getCenter();
        BuddyData existingBuddyData = buddyAnchors.get(spawnBlock);
        boolean respawn = (existingBuddyData == null || existingBuddyData.tryRespawning());

        if (!respawn)
            return;

        // Try respawning buddy !!!
        Buddy buddy = NMLEntities.BUDDY.get().create(level);
        if (buddy != null) {
            buddy.setPos(spawnBlock.above().getBottomCenter());

            // Prepare Buddy & Anchor
            buddy.prepareAnchor(spawnBlock);

            // Load Buddy NBT Data
            if (existingBuddyData != null && existingBuddyData.getNBTData().isPresent()) {
                // Save original buddy data as fallback
                CompoundTag fallbackTag = new CompoundTag();
                buddy.save(fallbackTag);

                // Replace data with previously saved buddy data
                CompoundTag replacementData = existingBuddyData.getNBTData().get();
                for (String key : replacementData.getAllKeys())
                    fallbackTag.put(key, Objects.requireNonNull(replacementData.get(key)));
                buddy.load(fallbackTag);
            }

            // Replace last anchor
            updateAnchors(spawnBlock, createData());
            level.addFreshEntity(buddy);
        }
    }

    public void updateAnchors(BlockPos anchorPosition, BuddyData newData) {
        buddyAnchors.put(anchorPosition, newData);
        setDirty();
    }

    private BuddyData createData() {
        int lastMoonCycle = getCurrentMoonCycle(level.dayTime());
        return new BuddyData(lastMoonCycle);
    }

    public void queryRespawn(BlockPos anchorPosition, Buddy buddy) {
        BuddyData buddyData = createData();
        buddyData.queryRespawn();

        CompoundTag buddySaveData = new CompoundTag();
        buddy.save(buddySaveData);

        CompoundTag storedSaveData = new CompoundTag();
        for (String copiedKey : Buddy.COPY_ON_RESPAWN) {
            if (buddySaveData.contains(copiedKey) && buddySaveData.get(copiedKey) != null)
                storedSaveData.put(copiedKey, Objects.requireNonNull(buddySaveData.get(copiedKey)));
        }
        buddyData.setNBTData(storedSaveData);
        // signal update to position
        updateAnchors(anchorPosition, buddyData);
    }
}
