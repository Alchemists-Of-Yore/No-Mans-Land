package com.farcr.nomansland;

import com.farcr.nomansland.common.event.CreativeModeTabHandler;
import com.farcr.nomansland.common.integration.BBIntegration;
import com.farcr.nomansland.common.integration.BoatloadIntegration;
import com.farcr.nomansland.common.integration.FDIntegration;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.registry.*;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.blocks.NMLExtinguishables;
import com.farcr.nomansland.common.registry.blocks.NMLFlammables;
import com.farcr.nomansland.common.registry.entities.*;
import com.farcr.nomansland.common.registry.items.NMLCreativeTabs;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.farcr.nomansland.common.registry.worldgen.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NoMansLand.MODID)
public class NoMansLand {

    public static final String MODID = "nomansland";
    public static final Logger LOGGER = LogManager.getLogger();

    public NoMansLand(IEventBus modEventBus, ModContainer modContainer) {

        NMLItems.ITEMS.register(modEventBus);
        NMLBlocks.BLOCKS.addAlias(NoMansLand.location("apple_fruit"), NoMansLand.location("apple"));
        NMLBlocks.BLOCKS.addAlias(NoMansLand.location("pear_fruit"), NoMansLand.location("pear"));
        NMLBlocks.BLOCKS.register(modEventBus);
        NMLExtinguishables.EXTINGUISHABLES.register(modEventBus);
        NMLEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(modEventBus);
        NMLEntities.ENTITIES.register(modEventBus);
        NMLSensors.SENSORS.register(modEventBus);
        NMLMemoryModules.MEMORY_MODULES.register(modEventBus);
        NMLFeatures.FEATURES.register(modEventBus);
        NMLFoliagePlacerTypes.FOLIAGE_PLACER_TYPES.register(modEventBus);
        NMLTrunkPlacerTypes.TRUNK_PLACER_TYPES.register(modEventBus);
        NMLSounds.SOUND_EVENTS.register(modEventBus);
        NMLCreativeTabs.CREATIVE_TABS.register(modEventBus);
        NMLParticleTypes.PARTICLE_TYPES.register(modEventBus);
        NMLBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        NMLLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        NMLTreeDecoratorTypes.TREE_DECORATOR_TYPES.register(modEventBus);
        NMLPondDecoratorTypes.POND_DECORATOR_TYPES.register(modEventBus);
        NMLBoulderDecoratorTypes.BOULDER_DECORATOR_TYPES.register(modEventBus);
        NMLFallenTreeDecoratorTypes.FALLEN_TREE_DECORATOR_TYPES.register(modEventBus);
        NMLFogModifiers.FOG_MODIFIERS.register(modEventBus);
        NMLMobVariants.FROG_VARIANTS.register(modEventBus);
        NMLMobVariants.MOB_VARIANT_TYPES.register(modEventBus);
        NMLEffects.MOB_EFFECTS.register(modEventBus);
        NMLStructureProcessorTypes.STRUCTURE_PROCESSOR_TYPES.register(modEventBus);
        NMLCriteriaTriggers.TRIGGERS.register(modEventBus);
        NMLRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        NMLRecipeSerializers.RECIPE_TYPES.register(modEventBus);
        NMLFluids.FLUID_TYPES.register(modEventBus);
        NMLFluids.FLUIDS.register(modEventBus);
        NMLBiomeModifiers.BIOME_MODIFIERS.register(modEventBus);
        NMLPlacementModifiers.PLACEMENT_MODIFIER_TYPES.register(modEventBus);
        NMLDensityFunctions.DENSITY_FUNCTIONS.register(modEventBus);

        if (Mods.FARMERSDELIGHT.isLoaded()) {
            FDIntegration.register();
            modEventBus.addListener(FDIntegration::addBlockEntities);
            NeoForge.EVENT_BUS.addListener(FDIntegration::onFruitCakeInteraction);
        }

        if (Mods.BLOCKBOX.isLoaded()) BBIntegration.register();
        if (Mods.BOATLOAD.isLoaded()) BoatloadIntegration.register();

        modEventBus.register(new CreativeModeTabHandler());
        modEventBus.addListener(NMLBlockEntities::addBlockEntities);

        modContainer.registerConfig(ModConfig.Type.COMMON, NMLConfig.COMMON_CONFIG);
        modContainer.registerConfig(ModConfig.Type.CLIENT, NMLConfig.CLIENT_CONFIG);
    }
    
    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, path);
    }
}
