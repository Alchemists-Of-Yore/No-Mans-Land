package com.farcr.nomansland.common.friend.dialogue;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class DialogueUtil {
    public static final int FRIEND_MOON_TEXT_COLOR = 9276752;
    public static Registry<DialogueRegistry.DialoguePool> getDialogueRegistry(Level level, ResourceKey<Registry<DialogueRegistry.DialoguePool>> resourceKey) {
        try {
            return level.registryAccess().registryOrThrow(resourceKey);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Error obtaining dialogue registry: " + e);
        }
    }

    /* It's probably fine to make this recursive? lol??? */
    public static DialogueRegistry.DialoguePool getWeightedEntry(WeightedRandomList<DialogueRegistry.DialoguePool> list, RandomSource randomSource) {
        Optional<DialogueRegistry.DialoguePool> optionalDialogue = list.getRandom(randomSource);
        return optionalDialogue.orElseGet(() -> getWeightedEntry(list, randomSource));
    }

    public static <T> void appendTags(
            T value, RegistryAccess registryAccess,
            ResourceKey<Registry<T>> registry,
            HashMap<T, ArrayList<DialogueRegistry.DialoguePool>> map,
            HashMap<TagKey<T>, ArrayList<DialogueRegistry.DialoguePool>> tagMap,
            ArrayList<DialogueRegistry.DialoguePool> emptyPool
    ) {
        if (map.containsKey(value))
            emptyPool.addAll(map.get(value));

        Optional<Registry<T>> optionalRegistry = registryAccess.registry(registry);
        if (optionalRegistry.isPresent()) {
            Registry<T> obtainedRegistry = optionalRegistry.get();
            Optional<Holder.Reference<T>> holder =
                obtainedRegistry.getHolder(obtainedRegistry.getKey(value));
            if (holder.isPresent()) {
                for (TagKey<T> tag : holder.get().tags().toList()) {
                    if (tagMap.containsKey(tag))
                        emptyPool.addAll(tagMap.get(tag));
                }
            }
        }
    }
}
