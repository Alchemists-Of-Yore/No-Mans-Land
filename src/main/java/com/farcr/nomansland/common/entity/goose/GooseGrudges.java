package com.farcr.nomansland.common.entity.goose;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GooseGrudges {
    public static final int MAX_GRUDGES = 5;

    private static final String LIST_KEY = "Grudges";
    private static final String TARGET_KEY = "Target";
    private static final String OFFERINGS_KEY = "Offerings";
    private static final float FORGIVENESS_STEP = 0.25F;

    private final Map<UUID, Integer> offeringsByTarget = new LinkedHashMap<>();

    public boolean holdsGrudgeAgainst(UUID target) {
        return offeringsByTarget.containsKey(target);
    }

    public boolean isFull() {
        return offeringsByTarget.size() >= MAX_GRUDGES;
    }

    public Set<UUID> targets() {
        return Collections.unmodifiableSet(offeringsByTarget.keySet());
    }

    public void hold(UUID target) {
        if (!offeringsByTarget.containsKey(target) && !isFull()) {
            offeringsByTarget.put(target, 0);
        }
    }

    public boolean offerPeace(UUID target, RandomSource random) {
        Integer offerings = offeringsByTarget.get(target);
        if (offerings == null) return false;

        int total = offerings + 1;
        if (random.nextFloat() < (total - 1) * FORGIVENESS_STEP) {
            offeringsByTarget.remove(target);
            return true;
        }
        offeringsByTarget.put(target, total);
        return false;
    }

    public void save(CompoundTag tag) {
        ListTag list = new ListTag();
        offeringsByTarget.forEach((target, offerings) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(TARGET_KEY, target);
            entry.putInt(OFFERINGS_KEY, offerings);
            list.add(entry);
        });
        tag.put(LIST_KEY, list);
    }

    public void load(CompoundTag tag) {
        offeringsByTarget.clear();
        ListTag list = tag.getList(LIST_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size() && i < MAX_GRUDGES; i++) {
            CompoundTag entry = list.getCompound(i);
            offeringsByTarget.put(entry.getUUID(TARGET_KEY), entry.getInt(OFFERINGS_KEY));
        }
    }
}
