package com.farcr.nomansland.data.assets;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Lang extends LanguageProvider {
    public Lang(PackOutput output) {
        super(output, NoMansLand.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        Set<Item> items = BuiltInRegistries.ITEM.stream().filter(i -> NoMansLand.MODID.equals(BuiltInRegistries.ITEM.getKey(i).getNamespace()))
                .collect(Collectors.toSet());

        items.remove(NMLItems.NO_MANS_GLOBE.get());

        takeAll(items, item -> item instanceof BlockItem);
        items.forEach(item -> add(item, getLangName(item.toString())));

        Set<Block> blocks = BuiltInRegistries.BLOCK.stream().filter(i -> NoMansLand.MODID.equals(BuiltInRegistries.BLOCK.getKey(i).getNamespace()))
                .collect(Collectors.toSet());

        blocks.forEach(block -> add(block, getLangName(block.asItem().toString())));

        add("itemGroup.nomansland", "No Man's Land");
        add(NMLItems.NO_MANS_GLOBE.get(), "No Man's Globe");
    }

    public String getLangName(String id) {
        String[] words = id.toString().split(":")[1].split("_");

        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return result.toString().trim();
    }

    public static <T> Collection<T> takeAll(Set<T> src, Predicate<T> pred) {
        List<T> ret = new ArrayList<>();

        Iterator<T> iter = src.iterator();
        while (iter.hasNext()) {
            T item = iter.next();
            if (pred.test(item)) {
                iter.remove();
                ret.add(item);
            }
        }

        if (ret.isEmpty()) {
            NoMansLand.LOGGER.warn("takeAll predicate yielded nothing", new Throwable());
        }
        return ret;
    }
}
