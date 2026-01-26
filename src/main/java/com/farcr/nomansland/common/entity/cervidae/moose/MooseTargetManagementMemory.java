package com.farcr.nomansland.common.entity.cervidae.moose;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.*;

import java.util.*;
import java.util.concurrent.atomic.*;

public class MooseTargetManagementMemory {

    public final static int UPSET_DURATION = 48000; //2 Days

    public final Object2IntOpenHashMap<UUID> upsetDurations = new Object2IntOpenHashMap<>();

    public void addTarget(Entity target) {
        upsetDurations.put(target.getUUID(), UPSET_DURATION);
    }

    public void clearAggression(Entity target) {
        upsetDurations.removeInt(target.getUUID());
    }

    public boolean isUpsetAt(Entity entity) {
        return upsetDurations.containsKey(entity.getUUID());
    }

    public void tick() {
        var toRemove = new ArrayList<UUID>();
        for (Object2IntMap.Entry<UUID> entry : upsetDurations.object2IntEntrySet()) {
            entry.setValue(entry.getIntValue()-1);
            if (entry.getIntValue() <= 0) {
                toRemove.add(entry.getKey());
            }
        }
        for (UUID uuid : toRemove) {
            upsetDurations.removeInt(uuid);
        }
    }

    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();

        var index = new AtomicInteger();
        upsetDurations.forEach(((uuid, duration) -> {
            var id = "target_" + index.getAndAdd(1);
            tag.putUUID(id+"_uuid", uuid);
            tag.putInt(id+"_duration", duration);
        }));
        tag.putInt("target_count", index.get());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        upsetDurations.clear();

        int count = tag.getInt("target_count");

        for (int i = 0; i < count; i++) {
            var id = "target_" + i;
            var uuid = id + "_uuid";
            var duration = id + "_uuid";
            if (tag.contains(uuid) && tag.contains(duration)) {
                upsetDurations.put(tag.getUUID(uuid), tag.getInt(uuid));
            }
        }
    }
}
