package com.farcr.nomansland.common.block.pots;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public record PotVariant(PotSize size, ResourceLocation model, VoxelShape shape, List<PotTrait> traits) {
    public static final Codec<List<Double>> BOX_CODEC = Codec.DOUBLE.listOf(6, 6).comapFlatMap(
            list -> {
                if (!(list.getFirst() > list.get(3)) && !(list.get(1) > list.get(4)) && !(list.get(2) > list.getLast())) {
                    return DataResult.success(list);
                } else return DataResult.error(() -> "The minimum values need to be smaller or equal to the max values!");
            },
            box -> List.of(box.getFirst(), box.get(1), box.get(2), box.get(3), box.get(4), box.getLast())
    );

    public static final Codec<VoxelShape> VOXEL_SHAPE_CODEC = BOX_CODEC.listOf().xmap(boxes -> {
        VoxelShape shape = Shapes.empty();
        for (List<Double> box : boxes) {
            shape = Shapes.or(shape, Shapes.box(box.getFirst() / 16, box.get(1) / 16, box.get(2) / 16, box.get(3) / 16, box.get(4) / 16, box.getLast() / 16));
        }

        return shape;
    }, shape -> {
        List<List<Double>> boxes = new ArrayList<>();
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> boxes.add(List.of(minX, minY, minZ, maxX, maxY, maxZ)));
        return boxes;
    });

    public static final Codec<PotVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      PotSize.CODEC.fieldOf("size").forGetter(PotVariant::size),
      ResourceLocation.CODEC.fieldOf("model").forGetter(PotVariant::model),
      VOXEL_SHAPE_CODEC.fieldOf("shape").forGetter(PotVariant::shape),
      PotTrait.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(PotVariant::traits)
    ).apply(instance, PotVariant::new));
}
