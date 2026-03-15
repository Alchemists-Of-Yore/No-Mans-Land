package com.farcr.nomansland;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

public class CenteredSinglePoolElement extends SinglePoolElement {
    protected CenteredSinglePoolElement(Either<ResourceLocation, StructureTemplate> p_210348_, Holder<StructureProcessorList> p_210349_, StructureTemplatePool.Projection p_210350_, Optional<LiquidSettings> p_352209_) {
        super(p_210348_, p_210349_, p_210350_, p_352209_);
    }

    public Vec3i getOffset(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation) {
        BoundingBox boundingBox = this.getBoundingBox(structureTemplateManager, pos, rotation);
        int dx = boundingBox.getXSpan() / 2;
        int dz = boundingBox.getZSpan() / 2;
        Vec3i bbOff = switch (rotation) {
            case NONE -> new Vec3i(dx-1, 0, dz-1);
            case CLOCKWISE_90 -> new Vec3i(-dx, 0, dz-1);
            case CLOCKWISE_180 -> new Vec3i(-dx, 0, -dz);
            case COUNTERCLOCKWISE_90 -> new Vec3i(dx-1, 0, -dz);
        };
        return new Vec3i(7, 0, 7).subtract(bbOff);
    }

    public static class Type implements StructurePoolElementType<CenteredSinglePoolElement> {
        public static final MapCodec<CenteredSinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec())
                        .apply(instance, CenteredSinglePoolElement::new
                        )
        );

        @Override
        public MapCodec<CenteredSinglePoolElement> codec() {
            return CODEC;
        }
    }
}
