package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.block.pots.PotionTable;
import com.farcr.nomansland.common.block.tap.TapInteraction;
import com.farcr.nomansland.common.blockentity.BombDispenseBehavior;
import com.farcr.nomansland.common.commands.DreamCommand;
import com.farcr.nomansland.common.commands.MeetingPointCommand;
import com.farcr.nomansland.common.commands.SunDogCommand;
import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.entity.Buried;
import com.farcr.nomansland.common.entity.billhook_bass.BillhookBass;
import com.farcr.nomansland.common.entity.cave_carp.CaveCarp;
import com.farcr.nomansland.common.entity.beetle.Beetle;
import com.farcr.nomansland.common.entity.beetle.Grub;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.farcr.nomansland.common.entity.buddy.BuddyFood;
import com.farcr.nomansland.common.entity.centipede.Centipede;
import com.farcr.nomansland.common.entity.cervidae.deer.Deer;
import com.farcr.nomansland.common.entity.clod.Clod;
import com.farcr.nomansland.common.entity.frienderman.Frienderman;
import com.farcr.nomansland.common.entity.goose.Goose;
import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import com.farcr.nomansland.common.entity.remnant.Remnant;
import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import com.farcr.nomansland.common.friend.condition.DialogueConditionCompiler;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.integration.FDIntegration;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.create.CreateIntegration;
import com.farcr.nomansland.common.item.ThrowableBombItem;
import com.farcr.nomansland.common.mixin.BlockBehaviourAccessModifier;
import com.farcr.nomansland.common.networking.*;
import com.farcr.nomansland.common.networking.buddy.ClientboundBuddyCrouchPacket;
import com.farcr.nomansland.common.networking.buddy.ClientboundBuddyUpdateEffectsPacket;
import com.farcr.nomansland.common.friend.dialogue.DialogueTracker;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialoguePacket;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueRegistrySyncPacket;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueResetPacket;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueTrackerPacket;
import com.farcr.nomansland.common.networking.dream.ClientboundDimensionSyncPacket;
import com.farcr.nomansland.common.networking.dream.ClientboundDreamPacket;
import com.farcr.nomansland.common.networking.dream.ServerboundDreamAcknowledgePacket;
import com.farcr.nomansland.common.networking.friend.ClientboundMeetingPointPacket;
import com.farcr.nomansland.common.networking.friend.ClientboundMoonlightBasinTrackPacket;
import com.farcr.nomansland.common.networking.friend.FriendMoonUpdatePacket;
import com.farcr.nomansland.common.registry.NMLFluids;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.blocks.NMLFlammables;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.farcr.nomansland.common.world.generation.NMLBiomePlacements;
import com.farcr.nomansland.common.world.generation.NMLDensityModifications;
import com.farcr.nomansland.common.world.generation.NMLSurfaceRules;
import com.farcr.nomansland.common.world.orevein.OreVeinType;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

import static com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock.LEVEL;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class CommonSetupEvents {

    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (NMLConfig.BIOMES.get()) NMLBiomePlacements.register();
            NMLDensityModifications.register();
            NMLSurfaceRules.register();
            NMLFlammables.register();
            if (Mods.CREATE.isLoaded()) CreateIntegration.registerOpenPipeEffects();

            overrideMushroomSounds();

            for (BlockDefinition<?> definition : NMLBlocks.BLOCK_DEFINITIONS) {
                if (definition.get() instanceof FlowerPotBlock flowerPotBlock) {
                    flowerPotBlock.getEmptyPot().addPlant(BuiltInRegistries.BLOCK.getKey(flowerPotBlock.getPotted()), () -> flowerPotBlock);
                }
            }

            for (ItemDefinition<?> definition : NMLItems.ITEM_DEFINITIONS) {
                Item item = definition.item();
                if (item instanceof ThrowableBombItem) DispenserBlock.registerBehavior(item, new BombDispenseBehavior(item));
                else if (item instanceof ProjectileItem) DispenserBlock.registerProjectileBehavior(item);

                if (item instanceof BoatItem boat) DispenserBlock.registerBehavior(item, new BoatDispenseItemBehavior(boat.type, boat.hasChest));
            }
        });
    }

    private static void overrideMushroomSounds() {
        setSoundType(Blocks.RED_MUSHROOM, NMLSounds.MUSHROOM_CAP);
        setSoundType(Blocks.BROWN_MUSHROOM, NMLSounds.MUSHROOM_CAP);
        setSoundType(Blocks.RED_MUSHROOM_BLOCK, NMLSounds.MUSHROOM_CAP);
        setSoundType(Blocks.BROWN_MUSHROOM_BLOCK, NMLSounds.MUSHROOM_CAP);
        setSoundType(Blocks.MUSHROOM_STEM, NMLSounds.MUSHROOM_CAP);

        if (Mods.FARMERSDELIGHT.isLoaded()) FDIntegration.overrideMushroomColonySounds();
    }

    public static void setSoundType(Block block, SoundType soundType) {
        ((BlockBehaviourAccessModifier) block).nml$setSoundType(soundType);
    }

    @SubscribeEvent
    public static void registerRegistries(final NewRegistryEvent event) {
        event.register(NMLRegistries.POND_DECORATOR_TYPE);
        event.register(NMLRegistries.BOULDER_DECORATOR_TYPE);
        event.register(NMLRegistries.FALLEN_TREE_DECORATOR_TYPE);
        event.register(NMLRegistries.FOG_MODIFIERS);
        event.register(NMLRegistries.CONTEXTUAL_MUSIC);
        event.register(NMLRegistries.DREAM_TYPE);
        event.register(NMLRegistries.EXTINGUISHABLE_BLOCKS);
        event.register(NMLRegistries.DIALOGUE_CONDITIONAL_TYPE);
    }

    @SubscribeEvent
    public static void registerDatapackRegistries(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NMLRegistries.TAP_INTERACTION_KEY, TapInteraction.CODEC, TapInteraction.CODEC);
        event.dataPackRegistry(NMLRegistries.POT_VARIANT_KEY, PotVariant.CODEC, PotVariant.CODEC);
        event.dataPackRegistry(NMLRegistries.ORE_VEIN_KEY, OreVeinType.CODEC, null);

        /* Moonlight Dialogue Registry */
        event.dataPackRegistry(NMLRegistries.GREETING_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.PASSIVE_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.NEGATIVE_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.OFFERING_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.CONTEXTUAL_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.LEAVING_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.SPECIAL_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);

        event.dataPackRegistry(NMLRegistries.BUDDY_FOOD_KEY, BuddyFood.CODEC, BuddyFood.CODEC);
        event.dataPackRegistry(NMLRegistries.POTION_TABLE_KEY, PotionTable.CODEC, null);
    }

    @SubscribeEvent
    public static void createEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(NMLEntities.BILLHOOK_BASS.get(), BillhookBass.createAttributes().build());
        event.put(NMLEntities.CAVE_CARP.get(), CaveCarp.createAttributes().build());
        event.put(NMLEntities.DEER.get(), Deer.createAttributes().build());
        // TODO: moose
//        event.put(NMLEntities.MOOSE.get(), Moose.createAttributes().build());
        event.put(NMLEntities.GOOSE.get(), Goose.createAttributes().build());
        event.put(NMLEntities.CLOD.get(), Clod.createAttributes().build());
        event.put(NMLEntities.TORTOISE.get(), Tortoise.createAttributes().build());
        event.put(NMLEntities.LIVING_POT.get(), LivingPot.createAttributes().build());
        event.put(NMLEntities.BUDDY.get(), Buddy.createAttributes().build());
        event.put(NMLEntities.FRIENDERMAN.get(), Frienderman.createAttributes().build());
        event.put(NMLEntities.BURIED.get(), Buried.createAttributes().build());
        event.put(NMLEntities.CENTIPEDE.get(), Centipede.createAttributes().build());
        event.put(NMLEntities.REMNANT.get(), Remnant.createAttributes().build());
        event.put(NMLEntities.BEETLE.get(), Beetle.createAttributes().build());
        event.put(NMLEntities.GRUB.get(), Grub.createAttributes().build());
        event.put(NMLEntities.DREAMING_PLAYER.get(), Mob.createMobAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(final RegisterSpawnPlacementsEvent event) {
        event.register(NMLEntities.BILLHOOK_BASS.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BillhookBass::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.CAVE_CARP.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CaveCarp::checkCaveCarpSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.DEER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Deer::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // TODO: moose
//        event.register(NMLEntities.MOOSE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Moose::checkMooseSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityType.CAMEL, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Camel::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityType.HUSK, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.TORTOISE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Tortoise::checkTortoiseSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.GOOSE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.OCEAN_FLOOR, Goose::checkGooseSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.CLOD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Clod::checkClodSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.BEETLE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Beetle::checkBeetleSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.GRUB.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Grub::checkGrubSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.CENTIPEDE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Centipede::checkCentipedeSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.REMNANT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Remnant::checkRemnantSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void registerCauldronFluidContent(final RegisterCauldronFluidContentEvent event) {
        event.register(NMLBlocks.RESIN_OIL_CAULDRON.get(), NMLFluids.RESIN_OIL.get(), 1000, LEVEL);
        if (NeoForgeMod.MILK.isBound())
            event.register(NMLBlocks.MILK_CAULDRON.get(), NeoForgeMod.MILK.get(), 1000, LEVEL);
        if (Mods.CREATE.isLoaded())
            event.register(NMLBlocks.HONEY_CAULDRON.get(), Mods.CREATE.getFluid("honey"), 1000, LEVEL);
    }

    @SubscribeEvent
    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.WATER, NMLItems.AWKWARD_RESIDUE.get(), Potions.AWKWARD);
        event.getBuilder().addMix(Potions.AWKWARD, NMLItems.TRANSLUCENT_SAC.get(), Potions.INVISIBILITY);

        event.getBuilder().addRecipe(new AwkwardResidueDowngradeRecipe());
        event.getBuilder().addRecipe(new BandageInfusionRecipe());
    }

    private static boolean isEmptyBandage(ItemStack stack) {
        if (!stack.is(NMLItems.BANDAGE)) return false;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents == null || !contents.getAllEffects().iterator().hasNext();
    }

    private static boolean isPotionWithEffects(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.getAllEffects().iterator().hasNext();
    }

    private static boolean isUpgradedPotion(PotionContents contents) {
        return contents.potion().map(holder -> {
            String path = Objects.requireNonNull(holder.getKey()).location().getPath();
            return path.startsWith("strong_") || path.startsWith("long_");
        }).orElse(false);
    }

    private static Optional<Holder<Potion>> getBasePotionFromUpgraded(PotionContents contents) {
        return contents.potion().flatMap(holder -> {
            String path = Objects.requireNonNull(holder.getKey()).location().getPath();
            String basePath = null;
            if (path.startsWith("strong_")) {
                basePath = path.substring("strong_".length());
            } else if (path.startsWith("long_")) {
                basePath = path.substring("long_".length());
            }
            if (basePath != null) {
                ResourceLocation baseLocation = ResourceLocation.withDefaultNamespace(basePath);
                return BuiltInRegistries.POTION.getHolder(baseLocation);
            }
            return Optional.empty();
        });
    }

    private static class AwkwardResidueDowngradeRecipe implements IBrewingRecipe {
        @Override
        public boolean isInput(@NotNull ItemStack stack) {
            if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION)) return false;
            PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            return isUpgradedPotion(contents);
        }

        @Override
        public boolean isIngredient(@NotNull ItemStack stack) {
            return stack.is(NMLItems.AWKWARD_RESIDUE);
        }

        @Override
        public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
            if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
            PotionContents potionContents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            Optional<Holder<Potion>> basePotion = getBasePotionFromUpgraded(potionContents);
            if (basePotion.isEmpty()) return ItemStack.EMPTY;

            PotionContents newContents = new PotionContents(basePotion, potionContents.customColor(), potionContents.customEffects());
            ItemStack result = input.copy();
            result.set(DataComponents.POTION_CONTENTS, newContents);
            return result;
        }
    }

    private static class BandageInfusionRecipe implements IBrewingRecipe {
        @Override
        public boolean isInput(@NotNull ItemStack stack) {
            return stack.is(NMLItems.BANDAGE);
        }

        @Override
        public boolean isIngredient(@NotNull ItemStack stack) {
            return isPotionWithEffects(stack);
        }

        @Override
        public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
            if (!isEmptyBandage(input)) return ItemStack.EMPTY;

            PotionContents potionContents = ingredient.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null) return ItemStack.EMPTY;

            ItemStack result = new ItemStack(NMLItems.BANDAGE.get());
            result.set(DataComponents.POTION_CONTENTS, potionContents);
            return result;
        }
    }

    @SubscribeEvent
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");


        //Ominous Moose Behavior that sadly demands our own networking
        registrar.playToServer(ServerboundMooseBeginJumpSequencePacket.TYPE, ServerboundMooseBeginJumpSequencePacket.STREAM_CODEC, ServerboundMooseBeginJumpSequencePacket::handleData);

        // Dialogue Packet from Server
        registrar.playToClient(ClientboundDialoguePacket.TYPE, ClientboundDialoguePacket.STREAM_CODEC, ClientboundDialoguePacket::handleData);
        registrar.playToClient(ClientboundDialogueResetPacket.TYPE, ClientboundDialogueResetPacket.STREAM_CODEC, ClientboundDialogueResetPacket::handleData);
        registrar.playToClient(ClientboundDialogueRegistrySyncPacket.TYPE, ClientboundDialogueRegistrySyncPacket.STREAM_CODEC, ClientboundDialogueRegistrySyncPacket::handleData);
        registrar.playToClient(ClientboundDialogueTrackerPacket.TYPE, ClientboundDialogueTrackerPacket.STREAM_CODEC, ClientboundDialogueTrackerPacket::handleData);

        // Friend Moon related packets
        registrar.playToServer(FriendMoonUpdatePacket.ToServer.TYPE, FriendMoonUpdatePacket.ToServer.STREAM_CODEC, FriendMoonUpdatePacket.ToServer::handleData);
        registrar.playToClient(FriendMoonUpdatePacket.ToClient.TYPE, FriendMoonUpdatePacket.ToClient.STREAM_CODEC, FriendMoonUpdatePacket.ToClient::handleData);

        registrar.playToClient(ClientboundMoonlightBasinTrackPacket.TYPE, ClientboundMoonlightBasinTrackPacket.STREAM_CODEC, ClientboundMoonlightBasinTrackPacket::handleData);
        registrar.playToClient(ClientboundMeetingPointPacket.TYPE, ClientboundMeetingPointPacket.STREAM_CODEC, ClientboundMeetingPointPacket::handleData);
        registrar.playToClient(ClientboundCandleLightPacket.TYPE, ClientboundCandleLightPacket.STREAM_CODEC, ClientboundCandleLightPacket::handleData);

        /* Dream Packets */
        registrar.playToClient(ClientboundDreamPacket.TYPE, ClientboundDreamPacket.STREAM_CODEC, ClientboundDreamPacket::handleData);
        registrar.playToClient(ClientboundDimensionSyncPacket.TYPE, ClientboundDimensionSyncPacket.STREAM_CODEC, ClientboundDimensionSyncPacket::handleData);
        registrar.playToServer(ServerboundDreamAcknowledgePacket.TYPE, ServerboundDreamAcknowledgePacket.STREAM_CODEC, ServerboundDreamAcknowledgePacket::handleData);

        registrar.playToClient(ClientboundBuddyCrouchPacket.TYPE, ClientboundBuddyCrouchPacket.STREAM_CODEC, ClientboundBuddyCrouchPacket::handleData);
        registrar.playToClient(ClientboundBuddyUpdateEffectsPacket.TYPE, ClientboundBuddyUpdateEffectsPacket.STREAM_CODEC, ClientboundBuddyUpdateEffectsPacket::handleData);

        registrar.playToClient(ClientboundZoomEffectPacket.TYPE, ClientboundZoomEffectPacket.STREAM_CODEC, ClientboundZoomEffectPacket::handleData);

        // sun dog update packet
        registrar.playToClient(ClientboundSunDogStatePacket.TYPE, ClientboundSunDogStatePacket.STREAM_CODEC, ClientboundSunDogStatePacket::handleData);

        registrar.playToClient(ClientboundInvertedBellPacket.TYPE, ClientboundInvertedBellPacket.STREAM_CODEC, ClientboundInvertedBellPacket::handleData);
        registrar.playToClient(ClientboundDistantChunkPacket.TYPE, ClientboundDistantChunkPacket.STREAM_CODEC, ClientboundDistantChunkPacket::handleData);

        registrar.playToClient(ClientboundBandageSoundPacket.TYPE, ClientboundBandageSoundPacket.STREAM_CODEC, ClientboundBandageSoundPacket::handleData);
    }

    @SubscribeEvent
    public static void registerListeners(RegisterCommandsEvent event) {
        DreamCommand.register(event.getDispatcher(), event.getBuildContext());
        SunDogCommand.register(event.getDispatcher());
        MeetingPointCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MeetingPointCommand.applyPersistedOverride(event.getServer());
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        event.addListener(new DialogueConditionCompiler(event.getRegistryAccess()));
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach((player) -> {
            PacketDistributor.sendToPlayer(player, new ClientboundDialogueRegistrySyncPacket());
            DialogueTracker.getOrDefault(player.server).sync(player);
        });
    }
}