package com.farcr.nomansland.datagen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class NMLLanguageProvider extends LanguageProvider {
    public NMLLanguageProvider(PackOutput output) {
        super(output, NoMansLand.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (BlockDefinition<?> definition : NMLBlocks.BLOCK_DEFINITIONS) {
            if (!definition.hasCustomLang()) {
                add(definition.langKey(), definition.langName());
            }
        }

        for (ItemDefinition<?> definition : NMLItems.ITEM_DEFINITIONS) {
            if (!definition.hasCustomLang() && !definition.isBlockItem()) {
                add(definition.langKey(), definition.langName());
            }
        }

        for (ResourceKey<Biome> biome : NMLBiomes.BIOMES) {
            add(NMLBiomes.langKey(biome), NMLBiomes.langName(biome));
        }

        add("item.nomansland.billhook_bass_bucket", "Bucket of Billhook Bass");
        add("item.nomansland.billhook_bass", "Raw Billhook Bass");
        add("item.nomansland.no_mans_globe", "No Man's Globe");
        add("item.nomansland.maple_chest_boat", "Maple Boat with Chest");
        add("item.nomansland.pine_chest_boat", "Pine Boat with Chest");
        add("item.nomansland.walnut_chest_boat", "Walnut Boat with Chest");
        add("item.nomansland.willow_chest_boat", "Willow Boat with Chest");
        add("item.nomansland.maple_furnace_boat", "Maple Boat with Furnace");
        add("item.nomansland.pine_furnace_boat", "Pine Boat with Furnace");
        add("item.nomansland.walnut_furnace_boat", "Walnut Boat with Furnace");
        add("item.nomansland.willow_furnace_boat", "Willow Boat with Furnace");
        add("itemGroup.nomansland", "No Man's Land");
        add("entity.nomansland.buried", "Buried");
        add("entity.nomansland.cave_carp", "Cave Carp");
        add("entity.nomansland.billhook_bass", "Billhook Bass");
        add("entity.nomansland.deer", "Deer");
        add("entity.nomansland.tortoise", "Tortoise");
        add("fluid_type.nomansland.resin_oil", "Resin Oil");
        add("nomansland.tooltip.mask.regeneration", "Slowly regenerates health");
        add("nomansland.subtitles.block.spike_trap.extend", "Spikes extend");
        add("nomansland.subtitles.block.spike_trap.retract", "Spikes retract");
        add("nomansland.subtitles.block.monster_anchor.monster_resurrection", "Monster begins resurrection");
        add("nomansland.subtitles.block.monster_anchor.activate", "Monster anchor activates");
        add("nomansland.subtitles.block.monster_anchor.deactivate", "Monster anchor deactivates");
        add("nomansland.subtitles.entity.player.drink_milk", "Player drinks milk");
        add("nomansland.subtitles.entity.generic.sticky_cauldron_slide", "Sliding in a sticky cauldron");
        add("nomansland.subtitles.item.generic.consumed", "Item consumed");
        add("nomansland.subtitles.item.bomb.primed", "Bomb primed");
        add("nomansland.subtitles.entity.billhook_bass.death", "Billhook Bass dies");
        add("nomansland.subtitles.entity.billhook_bass.flop", "Billhook Bass flops");
        add("nomansland.subtitles.entity.billhook_bass.hurt", "Billhook Bass hurts");
        add("nomansland.subtitles.entity.deer.death", "Deer dies");
        add("nomansland.subtitles.entity.deer.hurt", "Deer hurts");
        add("nomansland.subtitles.entity.deer.ambient", "Deer bleats");
        add("nomansland.subtitles.entity.goose.death", "Goose dies");
        add("nomansland.subtitles.entity.goose.hurt", "Goose hurts");
        add("nomansland.subtitles.entity.goose.ambient", "Goose honks");
        add("nomansland.subtitles.particle.droplet.fall", "Droplet falls");
        add("nomansland.subtitles.entity.living_urn.shatter", "Living Urn shatters");
        add("death.attack.nomansland.icicle_pierce", "%1$s was pierced by an icicle");
        add("death.attack.nomansland.icicle_pierce.player", "%1$s was pierced by an icicle while fighting %2$s");
        add("death.attack.nomansland.spike_fall", "%1$s fell for a spike trap");
        add("death.attack.nomansland.spike_fall.player", "%1$s fell for a spike trap while fighting %2$s");
        add("death.attack.nomansland.spike_impale", "%1$s was impaled in a spike trap");
        add("death.attack.nomansland.spike_impale.player", "%1$s was impaled in a spike trap while fighting %2$s");
        add("death.attack.nomansland.spike_poke", "%1$s got stuck in a spike trap");
        add("death.attack.nomansland.spike_poke.player", "%1$s got stuck in a spike trap while fighting %2$s");
        add("death.attack.nomansland.spike_skewer", "%1$s was skewered by a spike trap");
        add("death.attack.nomansland.spike_skewer.player", "%1$s was skewered by a spike trap while fighting %2$s");
        add("death.attack.nomansland.combust", "%1$s faced immolation");
        add("death.attack.nomansland.combust.player", "%1$s was immolated by %2$s");
        add("entity.nomansland.explosive", "Explosive");
        add("entity.nomansland.firebomb", "Firebomb");
        add("effect.nomansland.flammable", "Flammable");
        add("nomansland.advancements.use_tap.title", "Tree Juice");
        add("nomansland.advancements.use_tap.description", "Collect Resin or Maple Syrup with a Tap and a Cauldron");
        add("nomansland.advancements.kill_anchored_mob.title", "Can't Even Die Right");
        add("nomansland.advancements.kill_anchored_mob.description", "Defeat an enemy under the influence of a Monster Anchor");
        add("nomansland.advancements.when_pigs_fly.title", "When Pigs Fly");
        add("nomansland.advancements.when_pigs_fly.description", "Ride a pig down a 40 block fall without it taking damage");
        add("nomansland.advancements.ignite_flammable_enemy.title", "Grossly Incandescent");
        add("nomansland.advancements.ignite_flammable_enemy.description", "Set an enemy on fire after dousing it in Resin Oil");
        add("nomansland.advancements.explode_ore.title", "Gets The Job Done");
        add("nomansland.advancements.explode_ore.description", "Mine Ores with an Explosive instead of a Pickaxe");
        add("nomansland.advancements.find_ancient_city.title", "Blue Hum");
        add("nomansland.advancements.find_ancient_city.description", "Find an Ancient City");
        add("nomansland.advancements.find_mineshaft.title", "To Blisters and Bedrock");
        add("nomansland.advancements.find_mineshaft.description", "Find a Mineshaft");
        add("block.nomansland.resin_cauldron", "Cauldron Filled with Resin");
        add("block.nomansland.honey_cauldron", "Cauldron Filled with Honey");
        add("block.nomansland.maple_syrup_cauldron", "Cauldron Filled with Maple Syrup");
        add("block.nomansland.resin_oil_cauldron", "Cauldron Filled with Resin Oil");
        add("block.nomansland.milk_cauldron", "Cauldron Filled with Milk");
    }
}
