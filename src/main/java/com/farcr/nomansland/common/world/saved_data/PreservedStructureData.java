package com.farcr.nomansland.common.world.saved_data;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PreservedStructureData extends SavedData {
    public static final String NAME = "preserved_structures";

    private static final Map<ResourceKey<Level>, PreservedStructureData> CACHE = new ConcurrentHashMap<>();

    private final Long2IntOpenHashMap refCount = new Long2IntOpenHashMap();
    private final Long2ObjectOpenHashMap<LongOpenHashSet> byOwner = new Long2ObjectOpenHashMap<>();

    public static PreservedStructureData get(final ServerLevel level) {
        return CACHE.computeIfAbsent(level.dimension(), key -> level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PreservedStructureData::new, PreservedStructureData::load), NAME));
    }

    public static void prime(final ServerLevel level) {
        get(level);
    }

    public static void drop(final ServerLevel level) {
        CACHE.remove(level.dimension());
    }

    public synchronized boolean isPreserved(final long packedPos) {
        return this.refCount.get(packedPos) > 0;
    }

    public synchronized void register(final long owner, final long[] positions) {
        final LongOpenHashSet set = this.byOwner.computeIfAbsent(owner, key -> new LongOpenHashSet());
        boolean changed = false;
        for (final long pos : positions) {
            if (set.add(pos)) {
                this.refCount.addTo(pos, 1);
                changed = true;
            }
        }
        if (changed) this.setDirty();
    }

    public synchronized void unregister(final long owner) {
        final LongOpenHashSet set = this.byOwner.remove(owner);
        if (set == null) return;

        for (final LongIterator iterator = set.iterator(); iterator.hasNext(); ) {
            final long pos = iterator.nextLong();
            final int previous = this.refCount.addTo(pos, -1);
            if (previous - 1 <= 0) this.refCount.remove(pos);
        }
        this.setDirty();
    }

    public static PreservedStructureData load(final CompoundTag tag, final HolderLookup.Provider registries) {
        final PreservedStructureData data = new PreservedStructureData();
        final ListTag list = tag.getList("Owners", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            final CompoundTag entry = list.getCompound(i);
            final long owner = entry.getLong("Owner");
            final long[] positions = entry.getLongArray("Positions");
            if (positions.length == 0) continue;

            final LongOpenHashSet set = new LongOpenHashSet(positions);
            data.byOwner.put(owner, set);
            for (final long pos : positions) {
                data.refCount.addTo(pos, 1);
            }
        }

        return data;
    }

    @Override
    public synchronized CompoundTag save(final CompoundTag tag, final HolderLookup.Provider registries) {
        final ListTag list = new ListTag();

        for (final Long2ObjectMap.Entry<LongOpenHashSet> entry : this.byOwner.long2ObjectEntrySet()) {
            final CompoundTag ownerTag = new CompoundTag();
            ownerTag.putLong("Owner", entry.getLongKey());
            ownerTag.put("Positions", new LongArrayTag(entry.getValue().toLongArray()));
            list.add(ownerTag);
        }

        tag.put("Owners", list);
        return tag;
    }
}
