package com.farcr.nomansland.common.world.saved_data;

import com.farcr.nomansland.common.block.pots.PotData;
import com.farcr.nomansland.common.block.pots.PotModifier;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class RegeneratingPotsData extends SavedData {
    public final Long2ObjectMap<Pair<PotData, Integer>> regeneratingPots = new Long2ObjectOpenHashMap<>();
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

                this.regeneratingPots.put(pos.asLong(), Pair.of(potData, delay));
            }
        }

        return this;
    }

    @Override
    public CompoundTag save(final CompoundTag tag, final HolderLookup.Provider provider) {
        final ListTag listTag = new ListTag();

        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (final Long2ObjectMap.Entry<Pair<PotData, Integer>> entry : this.regeneratingPots.long2ObjectEntrySet()) {
            final BlockPos pos = mutableBlockPos.set(entry.getLongKey());
            final PotData potData = entry.getValue().getFirst();
            final Integer delay = entry.getValue().getSecond();

            final CompoundTag entryTag = new CompoundTag();
            entryTag.put("pos", NbtUtils.writeBlockPos(pos));
            entryTag.put("potData", potData.write());
            entryTag.put("delay", IntTag.valueOf(delay));
            listTag.add(entryTag);
        }

        tag.put("pots", listTag);

        return tag;
    }

    public void tick() {
        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        final var iter = this.regeneratingPots.long2ObjectEntrySet().iterator();

        while (iter.hasNext()) {
            final Long2ObjectMap.Entry<Pair<PotData, Integer>> entry = iter.next();
            final BlockPos pos = mutableBlockPos.set(entry.getLongKey());

            if (!this.level.getBlockState(pos).isAir()) {
                this.setDirty();
                iter.remove();
                continue;
            }

            if (entry.getValue().getSecond() <= 0) {
                final PotData potData = entry.getValue().getFirst();
                this.level.setBlockAndUpdate(pos, potData.state());
                if (this.level.getBlockEntity(pos) instanceof final PotBlockEntity pot) {
                    final Registry<PotVariant> variants = this.level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY);
                    pot.variant = variants.get(potData.variant());
                    for (final PotModifier mod : potData.modifiers()) {
                        pot.addModifier(mod);
                    }
                }

                this.setDirty();
                iter.remove();
                continue;
            }

            this.regeneratingPots.replace(pos.asLong(), Pair.of(entry.getValue().getFirst(), entry.getValue().getSecond() - 1));
            if (!this.isDirty()) {
                this.setDirty();
            }
        }
    }

    public void addPot(final BlockPos pos, final PotData data, final Integer delay) {
        this.regeneratingPots.put(pos.asLong(), Pair.of(data, delay));
        this.setDirty();
    }

    public void removePot(final BlockPos pos) {
        if (this.regeneratingPots.remove(pos.asLong()) != null) {
            this.setDirty();
        }
    }
}
