package com.farcr.nomansland.common.world.saved_data;

import com.farcr.nomansland.common.block.pots.PotData;
import com.farcr.nomansland.common.block.pots.PotModifier;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.NMLRegistries;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class RegeneratingPotsData extends SavedData {
    public static final class Entry {
        public PotData data;
        public int delay;

        public Entry(PotData data, int delay) {
            this.data = data;
            this.delay = delay;
        }
    }

    public final Long2ObjectMap<Entry> regeneratingPots = new Long2ObjectOpenHashMap<>();
    public final ServerLevel level;
    public static final String NAME = "regenerating_pots";

    public static RegeneratingPotsData getOrDefault(final ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                () -> new RegeneratingPotsData(level),
                (tag, provider) -> RegeneratingPotsData.create(tag, provider, level)
        ), RegeneratingPotsData.NAME);
    }

    public RegeneratingPotsData(final ServerLevel level) {
        this.level = level;
    }

    public static RegeneratingPotsData create(final CompoundTag tag, final HolderLookup.Provider provider, final ServerLevel serverLevel) {
        final RegeneratingPotsData data = new RegeneratingPotsData(serverLevel);
        return data.load(tag, provider);
    }

    public RegeneratingPotsData load(final CompoundTag tag, final HolderLookup.Provider registries) {
        this.regeneratingPots.clear();

        for (final Tag entryTag : tag.getList("pots", 10)) {
            if (entryTag instanceof final CompoundTag dataTag) {
                final BlockPos pos = NbtUtils.readBlockPos(dataTag, "pos").orElseThrow();
                final PotData potData = PotData.read(dataTag.getCompound("potData"), registries);
                final int delay = dataTag.getInt("delay");

                this.regeneratingPots.put(pos.asLong(), new Entry(potData, delay));
            }
        }

        return this;
    }

    @Override
    public CompoundTag save(final CompoundTag tag, final HolderLookup.Provider provider) {
        final ListTag listTag = new ListTag();

        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (final Long2ObjectMap.Entry<Entry> entry : this.regeneratingPots.long2ObjectEntrySet()) {
            final BlockPos pos = mutableBlockPos.set(entry.getLongKey());
            final Entry value = entry.getValue();

            final CompoundTag entryTag = new CompoundTag();
            entryTag.put("pos", NbtUtils.writeBlockPos(pos));
            entryTag.put("potData", value.data.write());
            entryTag.put("delay", IntTag.valueOf(value.delay));
            listTag.add(entryTag);
        }

        tag.put("pots", listTag);

        return tag;
    }

    public void tick() {
        if (this.regeneratingPots.isEmpty()) return;

        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        final var iter = this.regeneratingPots.long2ObjectEntrySet().iterator();
        boolean changed = false;

        while (iter.hasNext()) {
            final Long2ObjectMap.Entry<Entry> mapEntry = iter.next();
            final Entry entry = mapEntry.getValue();

            if (entry.delay > 0) {
                entry.delay--;
                continue;
            }

            final BlockPos pos = mutableBlockPos.set(mapEntry.getLongKey());
            if (!this.level.hasChunksAt(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) continue;

            if (this.level.getBlockState(pos).isAir()) {
                this.level.setBlockAndUpdate(pos, entry.data.state());
                if (this.level.getBlockEntity(pos) instanceof final PotBlockEntity pot) {
                    final Registry<PotVariant> variants = this.level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY);
                    final PotVariant restored = variants.get(entry.data.variant());
                    if (restored != null) {
                        pot.variant = restored;
                    }
                    for (final PotModifier mod : entry.data.modifiers()) {
                        pot.addModifier(mod);
                    }
                }
            }

            iter.remove();
            changed = true;
        }

        if (changed) this.setDirty();
    }

    public void addPot(final BlockPos pos, final PotData data, final int delay) {
        this.regeneratingPots.put(pos.asLong(), new Entry(data, delay));
        this.setDirty();
    }

    public void removePot(final BlockPos pos) {
        if (this.regeneratingPots.remove(pos.asLong()) != null) {
            this.setDirty();
        }
    }
}
