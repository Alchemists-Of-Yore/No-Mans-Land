package com.farcr.nomansland.common.block.fruit_trees;

import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum FruitType implements StringRepresentable {
    APPLE_OAK("apple_oak", NMLBlocks.APPLE_FRUIT, NMLBlocks.APPLE_FRUIT_LEAVES, Blocks.OAK_LEAVES.defaultBlockState().getBlockHolder(), 3, Items.APPLE.getDefaultInstance().getItemHolder(), new VoxelShape[]{
            Block.box(6, 11, 7, 10, 15, 11),
            Block.box(6, 11, 7, 10, 15, 11),
            Block.box(5.5, 10, 6.5, 10.5, 15, 11.5),
            Block.box(5.5, 10, 6.5, 10.5, 15, 11.5),
            Block.box(5.5, 10, 6.5, 10.5, 15, 11.5)
    }),
    PEAR_AUTUMNAL_OAK("pear_autumnal_oak", NMLBlocks.PEAR_FRUIT, NMLBlocks.PEAR_FRUIT_LEAVES, NMLBlocks.AUTUMNAL_OAK_LEAVES.get().defaultBlockState().getBlockHolder(), 3, NMLItems.PEAR, new VoxelShape[]{
            Block.box(6, 11, 7, 10, 15, 11),
            Block.box(6, 10, 7, 10, 15, 11),
            Shapes.or(Block.box(5.5, 9, 6.5, 10.5, 13, 11.5), Block.box(6.5, 13, 7.5, 9.5, 15, 10.5)),
            Shapes.or(Block.box(5.5, 9, 6.5, 10.5, 13, 11.5), Block.box(6.5, 13, 7.5, 9.5, 15, 10.5)),
            Shapes.or(Block.box(5.5, 9, 6.5, 10.5, 13, 11.5), Block.box(6.5, 13, 7.5, 9.5, 15, 10.5))
    });

    private final String name;
    private final Holder<Block> fruit;
    private final Holder<Block> fruitLeaves;
    private final Holder<Block> leaves;
    private final int growthSpeed;
    private final Holder<Item> fruitDrops;
    private final VoxelShape[] shapesByAge;

    public static final Codec<FruitType> CODEC = StringRepresentable.fromEnum(FruitType::values);

    FruitType(String name, Holder<Block> fruit, Holder<Block> fruitLeaves, Holder<Block> leaves, int growthSpeed, Holder<Item> fruitDrops, VoxelShape[] shapesByAge) {
        this.name = name;
        this.fruit = fruit;
        this.fruitLeaves = fruitLeaves;
        this.leaves = leaves;
        this.growthSpeed = growthSpeed;
        this.fruitDrops = fruitDrops;
        this.shapesByAge = shapesByAge;
    }

    public Holder<Block> getFruitBlock() {
        return fruit;
    }
    public Holder<Block> getFruitLeaves() {
        return fruitLeaves;
    }
    public Holder<Block> getLeaves() {
        return leaves;
    }
    public int getGrowthSpeed() {
        return growthSpeed;
    }
    public Holder<Item> getFruitDrops() {
        return fruitDrops;
    }
    public VoxelShape[] getShapesByAge() {
        return shapesByAge;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
