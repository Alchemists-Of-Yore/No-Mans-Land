package com.farcr.nomansland.common.integration;

import com.farcr.nomansland.common.block.CandleFruitCakeBlock;
import com.farcr.nomansland.common.block.FruitCakeBlock;
import com.farcr.nomansland.common.block.StallionStripsBlock;
import com.farcr.nomansland.common.block.cauldrons.EmptyWitchStewCauldron;
import com.farcr.nomansland.common.block.cauldrons.WitchStewBlockItem;
import com.farcr.nomansland.common.block.cauldrons.WitchStewCauldron;
import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.definitions.BlockProperties;
import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLFoods;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import vectorwing.farmersdelight.common.FoodValues;
import vectorwing.farmersdelight.common.block.CabinetBlock;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;
import vectorwing.farmersdelight.common.block.PieBlock;
import vectorwing.farmersdelight.common.item.ConsumableItem;
import vectorwing.farmersdelight.common.item.DrinkableItem;
import vectorwing.farmersdelight.common.item.MushroomColonyItem;
import vectorwing.farmersdelight.common.registry.ModBlockEntityTypes;
import vectorwing.farmersdelight.common.registry.ModEffects;

import static net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy;

@SuppressWarnings("unused")
public class FDIntegration {

    public static final BlockDefinition<CabinetBlock> PINE_CABINET = NMLBlocks.register("pine_cabinet",
            () -> new CabinetBlock(ofFullCopy(Blocks.BARREL)));

    public static final BlockDefinition<CabinetBlock> MAPLE_CABINET = NMLBlocks.register("maple_cabinet",
            () -> new CabinetBlock(ofFullCopy(Blocks.BARREL)));

    public static final BlockDefinition<CabinetBlock> WALNUT_CABINET = NMLBlocks.register("walnut_cabinet",
            () -> new CabinetBlock(ofFullCopy(Blocks.BARREL)));

    public static final BlockDefinition<CabinetBlock> WILLOW_CABINET = NMLBlocks.register("willow_cabinet",
            () -> new CabinetBlock(ofFullCopy(Blocks.BARREL)));

    public static final BlockDefinition<MushroomColonyBlock> FIELD_MUSHROOM_COLONY = NMLBlocks.registerNoItem("field_mushroom_colony",
            () -> new MushroomColonyBlock(NMLItems.FIELD_MUSHROOM, Block.Properties.ofFullCopy(NMLBlocks.FIELD_MUSHROOM.get())));

    public static final ItemDefinition<MushroomColonyItem> FIELD_MUSHROOM_COLONY_ITEM = NMLItems.register("field_mushroom_colony",
            () -> new MushroomColonyItem(FIELD_MUSHROOM_COLONY.get(), new Item.Properties()));

    public static final FoodProperties SEARED_VENISON_FOOD = new FoodProperties.Builder().nutrition(12).saturationModifier(0.9F).usingConvertsTo(Items.BOWL)
            .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT, 3600, 0, true, false), 1.0F).build();

    public static final ItemDefinition<ConsumableItem> SEARED_VENISON = NMLItems.register("seared_venison",
            () -> new ConsumableItem(new Item.Properties().food(SEARED_VENISON_FOOD).craftRemainder(Items.BOWL).stacksTo(16), true));

    public static final FoodProperties STALLION_STRIP_FOOD = new FoodProperties.Builder().nutrition(10).saturationModifier(0.8F)
            .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT, 3600, 0, true, false), 1.0F).build();

    public static final FoodProperties WITCH_STEW_FOOD = new FoodProperties.Builder().nutrition(8).saturationModifier(1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0, true, false), 0.1F)
            .effect(() -> new MobEffectInstance(ModEffects.COMFORT, 3600, 0, true, false), 1).build();

    public static final BlockDefinition<WitchStewCauldron> WITCH_STEW = NMLBlocks.registerNoItem("pot_of_witch_stew", WitchStewCauldron::new);

    public static final BlockDefinition<EmptyWitchStewCauldron> EMPTY_WITCH_STEW = NMLBlocks.registerNoItem("empty_pot_of_witch_stew",
            () -> new EmptyWitchStewCauldron(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)), BlockProperties.cauldron());

    public static final ItemDefinition<BlockItem> WITCH_STEW_BLOCK_ITEM = NMLItems.register("pot_of_witch_stew",
            () -> new WitchStewBlockItem(WITCH_STEW.block(), new Item.Properties().stacksTo(1)));

    public static final ItemDefinition<ConsumableItem> WITCH_STEW_ITEM = NMLItems.register("witch_stew",
            () -> new ConsumableItem(new Item.Properties().food(WITCH_STEW_FOOD).craftRemainder(Items.BOWL).stacksTo(16), true));

    public static final ItemDefinition<ConsumableItem> STALLION_STRIP = NMLItems.register("stallion_strip",
            () -> new ConsumableItem(new Item.Properties().food(STALLION_STRIP_FOOD).stacksTo(64), true));

    public static final BlockDefinition<StallionStripsBlock> STALLION_STRIPS_BLOCK = NMLBlocks.registerNoItem("stallion_strips",
            () -> new StallionStripsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));

    public static final ItemDefinition<BlockItem> STALLION_STRIPS_BLOCK_ITEM = NMLItems.register("stallion_strips",
            () -> new BlockItem(STALLION_STRIPS_BLOCK.block(), new Item.Properties().stacksTo(1)));

    public static final FoodProperties PASTA_WITH_PESTO_FOOD = new FoodProperties.Builder().nutrition(14).saturationModifier(0.75F)
            .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT, 6000, 0, true, false), 1).build();

    public static final ItemDefinition<ConsumableItem> PASTA_WITH_PESTO = NMLItems.register("pasta_with_pesto",
            () -> new ConsumableItem(new Item.Properties().food(PASTA_WITH_PESTO_FOOD).craftRemainder(Items.BOWL).stacksTo(16), true));

    public static final FoodProperties SALMON_AND_PESTO_GNOCCHI_FOOD = new FoodProperties.Builder().nutrition(16).saturationModifier(0.75F)
            .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT, 6000, 0, true, false), 1).build();

    public static final ItemDefinition<ConsumableItem> SALMON_AND_PESTO_GNOCCHI = NMLItems.register("salmon_and_pesto_gnocchi",
            () -> new ConsumableItem(new Item.Properties().food(SALMON_AND_PESTO_GNOCCHI_FOOD).craftRemainder(Items.BOWL).stacksTo(16), true));

    public static final ItemDefinition<Item> PEAR_COBBLER_SLICE = NMLItems.register("pear_cobbler_slice",
            () -> new Item(new Item.Properties().food(FoodValues.PIE_SLICE)));

    public static final BlockDefinition<PieBlock> PEAR_COBBLER = NMLBlocks.registerNoItem("pear_cobbler",
            () -> new PieBlock(ofFullCopy(Blocks.CAKE), PEAR_COBBLER_SLICE));

    public static final ItemDefinition<BlockItem> PEAR_COBBLER_ITEM = NMLItems.register("pear_cobbler",
            () -> new BlockItem(PEAR_COBBLER.get(), new Item.Properties()));

    public static final ItemDefinition<Item> FRUIT_CAKE_SLICE = NMLItems.register("fruit_cake_slice",
            () -> new Item(new Item.Properties().food(NMLFoods.FRUIT_CAKE_SLICE)));

    public static final BlockDefinition<CakeBlock> FRUIT_CAKE = NMLBlocks.registerNoItem("fruit_cake",
            () -> new FruitCakeBlock(ofFullCopy(Blocks.CAKE)));

    public static final BlockDefinition<CandleFruitCakeBlock> CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.CANDLE, ofFullCopy(FRUIT_CAKE.block()).lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 3 : 0)), BlockProperties.candleCake(Blocks.CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> WHITE_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("white_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.WHITE_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.WHITE_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> ORANGE_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("orange_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.ORANGE_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.ORANGE_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> MAGENTA_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("magenta_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.MAGENTA_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.MAGENTA_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> LIGHT_BLUE_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("light_blue_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.LIGHT_BLUE_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.LIGHT_BLUE_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> YELLOW_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("yellow_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.YELLOW_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.YELLOW_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> LIME_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("lime_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.LIME_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.LIME_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> PINK_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("pink_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.PINK_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.PINK_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> GRAY_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("gray_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.GRAY_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.GRAY_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> LIGHT_GRAY_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("light_gray_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.LIGHT_GRAY_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.LIGHT_GRAY_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> CYAN_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("cyan_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.CYAN_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.CYAN_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> PURPLE_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("purple_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.PURPLE_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.PURPLE_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> BLUE_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("blue_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.BLUE_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.BLUE_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> BROWN_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("brown_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.BROWN_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.BROWN_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> GREEN_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("green_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.GREEN_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.GREEN_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> RED_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("red_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.RED_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.RED_CANDLE));
    public static final BlockDefinition<CandleFruitCakeBlock> BLACK_CANDLE_FRUIT_CAKE = NMLBlocks.registerNoItem("black_candle_fruit_cake",
            () -> new CandleFruitCakeBlock(Blocks.BLACK_CANDLE, ofFullCopy(CANDLE_FRUIT_CAKE.block())), BlockProperties.candleCake(Blocks.BLACK_CANDLE));

    public static final ItemDefinition<BlockItem> FRUIT_CAKE_ITEM = NMLItems.register("fruit_cake",
            () -> new BlockItem(FRUIT_CAKE.get(), new Item.Properties()));

    public static final ItemDefinition<DrinkableItem> PEAR_JUICE = NMLItems.register("pear_juice",
            () -> new DrinkableItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16).food(FDIntegration.PEAR_JUICE_FOOD), true));

    public static final FoodProperties PEAR_JUICE_FOOD = new FoodProperties.Builder().nutrition(9).saturationModifier(1.0F).alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 300, 0), 1).build();

    public static final ItemDefinition<DrinkableItem> PESTO_BOTTLE = NMLItems.register("pesto_bottle",
            () -> new DrinkableItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16).food(FDIntegration.PESTO_BOTTLE_FOOD)));

    public static final FoodProperties PESTO_BOTTLE_FOOD = new FoodProperties.Builder().nutrition(3).saturationModifier(1.2F).build();

    public static void addBlockEntities(final BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                ModBlockEntityTypes.CABINET.get(),
                MAPLE_CABINET.get(),
                PINE_CABINET.get(),
                WALNUT_CABINET.get(),
                WILLOW_CABINET.get()
        );
    }

    public static void register() {
    }
}
