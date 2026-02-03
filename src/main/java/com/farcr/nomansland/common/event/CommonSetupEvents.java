package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry.DialoguePool;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.block.tap.TapInteraction;
import com.farcr.nomansland.common.blockentity.BombDispenseBehavior;
import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.friend.condition.DialogueConditionCompiler;
import com.farcr.nomansland.common.entity.billhook_bass.BillhookBass;
import com.farcr.nomansland.common.entity.cervidae.deer.Deer;
import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import com.farcr.nomansland.common.entity.goose.Goose;
import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.create.CreateIntegration;
import com.farcr.nomansland.common.item.ThrowableBombItem;
import com.farcr.nomansland.common.networking.ClientboundDialoguePacket;
import com.farcr.nomansland.common.networking.ClientboundMoonlightBasinTrackPacket;
import com.farcr.nomansland.common.networking.ServerboundFriendMoonUpdatePacket;
import com.farcr.nomansland.common.registry.NMLFluids;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.blocks.NMLFlammables;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.farcr.nomansland.common.world.generation.NMLBiomePlacements;
import com.farcr.nomansland.common.world.generation.NMLDensityModifications;
import com.farcr.nomansland.common.world.generation.NMLSurfaceRules;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent;
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

    @SubscribeEvent
    public static void registerRegistries(final NewRegistryEvent event) {
        event.register(NMLRegistries.POND_DECORATOR_TYPE);
        event.register(NMLRegistries.BOULDER_DECORATOR_TYPE);
        event.register(NMLRegistries.FALLEN_TREE_DECORATOR_TYPE);
        event.register(NMLRegistries.FOG_MODIFIERS);
        event.register(NMLRegistries.EXTINGUISHABLE_BLOCKS);
        event.register(NMLRegistries.DIALOGUE_CONDITIONAL_TYPE);
    }

    @SubscribeEvent
    public static void registerDatapackRegistries(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NMLRegistries.TAP_INTERACTION_KEY, TapInteraction.CODEC, TapInteraction.CODEC);
        event.dataPackRegistry(NMLRegistries.POT_VARIANT_KEY, PotVariant.CODEC, PotVariant.CODEC);

        /* Moonlight Dialogue Registry */
        event.dataPackRegistry(NMLRegistries.PASSIVE_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.NEGATIVE_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.OFFERING_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.CONTEXTUAL_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
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

    @SubscribeEvent
    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.WATER, NMLItems.AWKWARD_RESIDUE.get(), Potions.AWKWARD);

        event.getBuilder().addRecipe(new AwkwardResidueDowngradeRecipe());
        event.getBuilder().addRecipe(new BandageInfusionRecipe());
    }

    private static boolean isEmptyBandage(ItemStack stack) {
        if (!stack.is(NMLItems.BANDAGE)) return false;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents == null || !contents.getAllEffects().iterator().hasNext();
    }

    private static boolean isLevel1Potion(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        boolean hasEffects = false;
        for (MobEffectInstance effect : contents.getAllEffects()) {
            hasEffects = true;
            if (effect.getAmplifier() > 0) return false;
        }
        return hasEffects;
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
            if (stack.is(NMLItems.BANDAGE)) return false;
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
            if (input.is(NMLItems.BANDAGE)) return ItemStack.EMPTY;
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
            return isLevel1Potion(stack);
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
    public static void onServerTick(ServerTickEvent.Pre event) {
        ServerLevel level = event.getServer().overworld();
        FriendMoon.getOrDefault(level).tick();
    }

    @SubscribeEvent
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Dialogue Packet from Server
        registrar.playToClient(ClientboundDialoguePacket.TYPE, ClientboundDialoguePacket.STREAM_CODEC, ClientboundDialoguePacket::handleData);
        registrar.playToClient(ClientboundMoonlightBasinTrackPacket.TYPE, ClientboundMoonlightBasinTrackPacket.STREAM_CODEC, ClientboundMoonlightBasinTrackPacket::handleData);
        registrar.playToServer(ServerboundFriendMoonUpdatePacket.TYPE, ServerboundFriendMoonUpdatePacket.STREAM_CODEC, ServerboundFriendMoonUpdatePacket::handleData);
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        event.addListener(new DialogueConditionCompiler(event.getRegistryAccess()));
    }
}