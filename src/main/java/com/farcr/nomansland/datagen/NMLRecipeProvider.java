package com.farcr.nomansland.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.world.level.block.Blocks.*;
import static com.farcr.nomansland.common.registry.blocks.NMLBlocks.*;

public class NMLRecipeProvider extends RecipeProvider {
    public NMLRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // cobbled deepslate bricks
        stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS, COBBLED_DEEPSLATE_BRICKS, COBBLED_DEEPSLATE);
        stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS, COBBLED_DEEPSLATE_BRICK_STAIRS, COBBLED_DEEPSLATE, 1);
        stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS, COBBLED_DEEPSLATE_BRICK_SLAB, COBBLED_DEEPSLATE, 2);
        stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS, COBBLED_DEEPSLATE_BRICK_WALL, COBBLED_DEEPSLATE, 1);

        stoneStair(output, COBBLED_DEEPSLATE_BRICK_STAIRS, COBBLED_DEEPSLATE_BRICKS);
        stoneSlab(output, COBBLED_DEEPSLATE_BRICK_SLAB, COBBLED_DEEPSLATE_BRICKS);
        stoneWall(output, COBBLED_DEEPSLATE_BRICK_WALL, COBBLED_DEEPSLATE_BRICKS);
    }

    protected static void stoneSlab(RecipeOutput recipeOutput, ItemLike slab, ItemLike material) {
        slab(recipeOutput, slab, material);
        stonecutterResultFromBase(recipeOutput, RecipeCategory.BUILDING_BLOCKS, slab, material, 2);
    }
    protected static void stoneStair(RecipeOutput recipeOutput, ItemLike stair, ItemLike material) {
        stair(recipeOutput, stair, material);
        stonecutterResultFromBase(recipeOutput, RecipeCategory.BUILDING_BLOCKS, stair, material);
    }
    protected static void stoneWall(RecipeOutput recipeOutput, ItemLike wall, ItemLike material) {
        wall(recipeOutput, wall, material);
        stonecutterResultFromBase(recipeOutput, RecipeCategory.BUILDING_BLOCKS, wall, material);
    }

    protected static void slab(RecipeOutput recipeOutput, ItemLike slab, ItemLike material) {
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, slab, material);
    }
    protected static void stair(RecipeOutput recipeOutput, ItemLike stair, ItemLike material) {
        stairBuilder(stair, Ingredient.of(material)).unlockedBy(getHasName(material), has(material)).save(recipeOutput);
    }
    protected static void wall(RecipeOutput recipeOutput, ItemLike wall, ItemLike material) {
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, wall, material);
    }
}
