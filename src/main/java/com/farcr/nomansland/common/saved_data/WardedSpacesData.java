package com.farcr.nomansland.common.saved_data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class WardedSpacesData extends SavedData {
    public static final String NAME = "warded_spaces";
    public ArrayList<BlockPos> positions;
    public ArrayList<Integer> ranges;

    public WardedSpacesData(ArrayList<BlockPos> positions, ArrayList<Integer> ranges) {
        this.positions = positions;
        this.ranges = ranges;
    }

    public WardedSpacesData() {
        this.positions = new ArrayList<>();
        this.ranges = new ArrayList<>();
    }

    public static WardedSpacesData create(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        Arrays.stream(tag.getLongArray("positions")).forEachOrdered(pos -> positions.add(BlockPos.of(pos)));

        ArrayList<Integer> ranges = new ArrayList<>();
        Arrays.stream(tag.getIntArray("ranges")).forEachOrdered(ranges::add);

        return new WardedSpacesData(positions, ranges);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ArrayList<Long> positions = new ArrayList<>();
        this.positions.forEach(pos -> positions.add(pos.asLong()));
        if (!positions.isEmpty()) {
            tag.putLongArray("positions", positions);
            tag.putIntArray("ranges", ranges);
        }

        return tag;
    }

    public void addEffigy(BlockPos pos, int range) {
        removeEffigy(pos);
        positions.add(pos);
        ranges.add(range);
        setDirty();
    }

    public void removeEffigy(BlockPos pos) {
        if (positions.contains(pos)) {
            ranges.remove(positions.indexOf(pos));
            positions.remove(pos);
            setDirty();
        }
    }

    public boolean isWarded(BlockPos pos) {
        if (positions.contains(pos)) return true;

        for (BlockPos wardedPos : positions) {
            if (wardedPos.distSqr(pos) <= Mth.square(ranges.get(positions.indexOf(wardedPos)))) {
                return true;
            }
        }

        return false;
    }

    public Optional<BlockPos> getAffectingEffigyAt(BlockPos pos) {
        if (positions.contains(pos)) return Optional.of(pos);

        BlockPos closestEffigy = null;
        for (BlockPos wardedPos : positions) {
            if (wardedPos.distSqr(pos) <= Mth.square(ranges.get(positions.indexOf(wardedPos)))) {
                if (closestEffigy == null) closestEffigy = wardedPos;
                else if (wardedPos.distSqr(pos) < closestEffigy.distSqr(pos))
                    closestEffigy = wardedPos;
            }
        }

        return Optional.ofNullable(closestEffigy);
    }
}
