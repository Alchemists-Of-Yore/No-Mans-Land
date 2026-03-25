package com.farcr.nomansland.common.world.saved_data;

import com.farcr.nomansland.common.block.pots.PotData;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RegeneratingPotsData extends SavedData {
    public final Map<BlockPos, Pair<PotData, Integer>> regeneratingPots = new HashMap<>();
    public final ServerLevel level;
    public static final String NAME = "regenerating_pots";

    public static RegeneratingPotsData getOrDefault(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                () -> new RegeneratingPotsData(level),
                (tag, provider) -> RegeneratingPotsData.create(tag, provider, level)
        ), RegeneratingPotsData.NAME);
    }

    public RegeneratingPotsData(ServerLevel level) {
        this.level = level;
    }

    public static RegeneratingPotsData create(CompoundTag tag, HolderLookup.Provider provider, ServerLevel serverLevel) {
        RegeneratingPotsData data = new RegeneratingPotsData(serverLevel);
        return data.load(tag, provider);
    }

    public RegeneratingPotsData load(CompoundTag tag, HolderLookup.Provider registries) {
        regeneratingPots.clear();

        for (Tag entryTag : tag.getList("pots", 10)) {
            if (entryTag instanceof CompoundTag dataTag) {
                BlockPos pos = NbtUtils.readBlockPos(dataTag, "pos").orElseThrow();
                PotData potData = PotData.read(dataTag.getCompound("potData"), registries);
                int delay = dataTag.getInt("delay");

                regeneratingPots.put(pos, Pair.of(potData, delay));
            }
        }

        return this;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        for (Map.Entry<BlockPos, Pair<PotData, Integer>> entry : regeneratingPots.entrySet()) {
            BlockPos pos = entry.getKey();
            PotData potData = entry.getValue().getFirst();
            Integer delay = entry.getValue().getSecond();

            CompoundTag entryTag = new CompoundTag();
            entryTag.put("pos", NbtUtils.writeBlockPos(pos));
            entryTag.put("potData", potData.write());
            entryTag.put("delay", IntTag.valueOf(delay));
            listTag.add(entryTag);
        }

        tag.put("pots", listTag);

        return tag;
    }

    public void tick() {
        for (Map.Entry<BlockPos, Pair<PotData, Integer>> entry : new HashSet<>(regeneratingPots.entrySet())) {
            if (entry.getValue().getSecond() < 0) {
                if (level.getBlockState(entry.getKey()).isAir()) {
                    level.setBlockAndUpdate(entry.getKey(), entry.getValue().getFirst().state());
                    if (level.getBlockEntity(entry.getKey()) instanceof PotBlockEntity pot) {
                        Registry<PotVariant> variants = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY);
                        pot.variant = variants.get(entry.getValue().getFirst().variant());
                    }
                }
                removePot(entry.getKey());
            }

            regeneratingPots.replace(entry.getKey(), Pair.of(entry.getValue().getFirst(), entry.getValue().getSecond() - 1));
            if (!isDirty()) setDirty();
        }
    }

    public void addPot(BlockPos pos, PotData data, Integer delay) {
        regeneratingPots.put(pos, Pair.of(data, delay));
        setDirty();
    }

    public void removePot(BlockPos pos) {
        if (regeneratingPots.remove(pos) != null) {
            setDirty();
        }
    }
}
