package com.farcr.nomansland.common.world.saved_data;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Arrays;

public class WardedSpacesData extends SavedData {
    public static final String NAME = "warded_spaces";

    private static final String MAP_KEY = "warded_spaces";
    protected static final String DATA_VERSION_KEY = "data_version";
    protected static final int DATA_VERSION = 1;

    private static final Vector3d TEMP_POS_A = new Vector3d();
    private static final Vector3d TEMP_POS_B = new Vector3d();

    public Long2IntMap wardedSpaces = new Long2IntOpenHashMap();

    public WardedSpacesData() {
        this.wardedSpaces.defaultReturnValue(Integer.MAX_VALUE);
    }

    public static WardedSpacesData load(final CompoundTag tag, final HolderLookup.Provider lookupProvider) {
        final int dataVersion = tag.contains(DATA_VERSION_KEY) ? tag.getInt(DATA_VERSION_KEY) : 0;
        final WardedSpacesData data = new WardedSpacesData();

        if (dataVersion == 0) {
            // Load legacy WardedSpacesData
            final ArrayList<BlockPos> positions = new ArrayList<>();
            Arrays.stream(tag.getLongArray("positions")).forEachOrdered(pos -> positions.add(BlockPos.of(pos)));

            final ArrayList<Integer> ranges = new ArrayList<>();
            Arrays.stream(tag.getIntArray("ranges")).forEachOrdered(ranges::add);

            assert positions.size() == ranges.size();
            for (int i = 0; i < positions.size(); i++) {
                data.wardedSpaces.put(positions.get(i).asLong(), (int) ranges.get(i));
            }

            return data;
        }

        final CompoundTag mapTag = tag.getCompound(MAP_KEY);

        for (final String key : mapTag.getAllKeys()) {
            final long longKey = Long.parseLong(key);
            data.wardedSpaces.put(longKey, mapTag.getInt(key));
        }

        return data;
    }

    public static WardedSpacesData get(final ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(WardedSpacesData::new, WardedSpacesData::load), WardedSpacesData.NAME);
    }

    @Override
    public CompoundTag save(final CompoundTag tag, final HolderLookup.Provider registries) {
        tag.putInt(DATA_VERSION_KEY, DATA_VERSION);

        final CompoundTag mapTag = new CompoundTag();

        for (final Long2IntMap.Entry entry : this.wardedSpaces.long2IntEntrySet()) {
            mapTag.putInt(String.valueOf(entry.getLongKey()), entry.getIntValue());
        }

        tag.put(MAP_KEY, mapTag);

        return tag;
    }

    public void addEffigy(final BlockPos pos, final int range) {
        this.removeEffigy(pos);
        this.wardedSpaces.put(pos.asLong(), range);
        this.setDirty();
    }

    public void removeEffigy(final BlockPos pos) {
        if (this.wardedSpaces.remove(pos.asLong()) != Integer.MAX_VALUE) {
            this.setDirty();
        }
    }

    private long snapshotTick = Long.MIN_VALUE;
    private int snapshotSize = -1;

    private double[] snapshotX = new double[0];
    private double[] snapshotY = new double[0];
    private double[] snapshotZ = new double[0];

    private double[] snapshotRange = new double[0];

    private void refreshSnapshot(final Level level) {
        final long tick = level.getGameTime();
        if (this.snapshotTick == tick && this.snapshotSize == this.wardedSpaces.size()) {
            return;
        }

        this.snapshotTick = tick;
        this.snapshotSize = this.wardedSpaces.size();
        if (this.snapshotX.length != this.snapshotSize) {
            this.snapshotX = new double[this.snapshotSize];
            this.snapshotY = new double[this.snapshotSize];
            this.snapshotZ = new double[this.snapshotSize];
            this.snapshotRange = new double[this.snapshotSize];
        }

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final Vector3d raw = new Vector3d();
        final Vector3d projected = new Vector3d();
        int index = 0;
        for (final Long2IntMap.Entry entry : this.wardedSpaces.long2IntEntrySet()) {
            cursor.set(entry.getLongKey());
            JOMLConversion.atBottomCenterOf(cursor, raw);
            SableCompanion.INSTANCE.projectOutOfSubLevel(level, raw, projected);
            this.snapshotX[index] = projected.x;
            this.snapshotY[index] = projected.y;
            this.snapshotZ[index] = projected.z;
            this.snapshotRange[index] = Mth.square((double) entry.getIntValue());
            index++;
        }
    }

    public boolean isWarded(final Level level, final BlockPos pos) {
        if (this.wardedSpaces.isEmpty()) return false;
        if (this.wardedSpaces.containsKey(pos.asLong())) return true;

        this.refreshSnapshot(level);
        JOMLConversion.atBottomCenterOf(pos, TEMP_POS_B);
        SableCompanion.INSTANCE.projectOutOfSubLevel(level, TEMP_POS_B, TEMP_POS_A);

        for (int index = 0; index < this.snapshotSize; index++) {
            final double dx = TEMP_POS_A.x - this.snapshotX[index];
            final double dy = TEMP_POS_A.y - this.snapshotY[index];
            final double dz = TEMP_POS_A.z - this.snapshotZ[index];
            if (dx * dx + dy * dy + dz * dz <= this.snapshotRange[index]) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public Pair<BlockPos, Integer> getAffectingEffigyAt(final Level level, final BlockPos pos) {
        if (this.wardedSpaces.containsKey(pos.asLong())) {
            return Pair.of(pos, this.wardedSpaces.get(pos.asLong()));
        }

        final BlockPos.MutableBlockPos wardedPos = new BlockPos.MutableBlockPos();
        Pair<BlockPos, Integer> closestEffigyPair = null;
        double closestDistanceSquared = Double.MAX_VALUE;

        for (final Long2IntMap.Entry entry : this.wardedSpaces.long2IntEntrySet()) {
            final int range = entry.getIntValue();
            wardedPos.set(entry.getLongKey());

            final Vector3d vecA = JOMLConversion.atBottomCenterOf(wardedPos, TEMP_POS_A);
            final Vector3d vecB = JOMLConversion.atBottomCenterOf(pos, TEMP_POS_B);

            final double distanceSquared = SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, vecA, vecB);

            // If we're in range
            if (distanceSquared <= Mth.square(range) && distanceSquared < closestDistanceSquared) {
                closestEffigyPair = Pair.of(wardedPos, range);
                closestDistanceSquared = distanceSquared;
            }
        }

        return closestEffigyPair;
    }
}
