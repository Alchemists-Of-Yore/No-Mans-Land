package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.tap.TapInteraction;
import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.entity.billhook_bass.BillhookBass;
import com.farcr.nomansland.common.entity.deer.Deer;
import com.farcr.nomansland.common.entity.goose.Goose;
import com.farcr.nomansland.common.entity.moose.Moose;
import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.registry.NMLFluids;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.blocks.NMLFlammables;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.world.generation.NMLBiomePlacements;
import com.farcr.nomansland.common.world.generation.NMLDensityModifications;
import com.farcr.nomansland.common.world.generation.NMLSurfaceRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import static com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock.LEVEL;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = NoMansLand.MODID)
public class CommonSetupEvents {

    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (NMLConfig.BIOMES.get()) NMLBiomePlacements.register();
            NMLDensityModifications.register();
            NMLSurfaceRules.register();
            NMLFlammables.register();

            for (BlockDefinition<?> definition : NMLBlocks.BLOCK_DEFINITIONS) {
                if (definition.get() instanceof FlowerPotBlock flowerPotBlock) {
                    flowerPotBlock.getEmptyPot().addPlant(BuiltInRegistries.BLOCK.getKey(flowerPotBlock.getPotted()), () -> flowerPotBlock);
                }
            }
        });
    }

    @SubscribeEvent
    public static void registerRegistries(final NewRegistryEvent event) {
        event.register(NMLRegistries.POND_DECORATOR_TYPE);
        event.register(NMLRegistries.BOULDER_DECORATOR_TYPE);
        event.register(NMLRegistries.FALLEN_TREE_DECORATOR_TYPE);
        event.register(NMLRegistries.FOG_MODIFIERS);

        event.register(NMLRegistries.EXTINGUISHABLE_BLOCKS);
    }

    @SubscribeEvent
    public static void registerDatapackRegistries(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NMLRegistries.TAP_INTERACTION_KEY, TapInteraction.DIRECT_CODEC, TapInteraction.DIRECT_CODEC);
    }

    @SubscribeEvent
    public static void createEntityAttributes(final EntityAttributeCreationEvent event) {
        event.put(NMLEntities.MOOSE.get(), Moose.createAttributes().build());
        event.put(NMLEntities.BILLHOOK_BASS.get(), BillhookBass.createAttributes().build());
        event.put(NMLEntities.DEER.get(), Deer.createAttributes().build());
        event.put(NMLEntities.GOOSE.get(), Goose.createAttributes().build());
        event.put(NMLEntities.TORTOISE.get(), Tortoise.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(final RegisterSpawnPlacementsEvent event) {
        event.register(NMLEntities.BILLHOOK_BASS.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BillhookBass::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.DEER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Deer::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityType.CAMEL, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Camel::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityType.HUSK, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.TORTOISE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Tortoise::checkTortoiseSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.GOOSE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.OCEAN_FLOOR, Goose::checkGooseSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void registerCauldronFluidContent(final RegisterCauldronFluidContentEvent event) {
        event.register(NMLBlocks.RESIN_OIL_CAULDRON.get(), NMLFluids.RESIN_OIL.get(), 1000, LEVEL);
        if (NeoForgeMod.MILK.isBound())
            event.register(NMLBlocks.MILK_CAULDRON.get(), NeoForgeMod.MILK.get(), 1000, LEVEL);
        if (Mods.CREATE.isLoaded())
            event.register(NMLBlocks.HONEY_CAULDRON.get(), Mods.CREATE.getFluid("honey"), 1000, LEVEL);
    }
}
