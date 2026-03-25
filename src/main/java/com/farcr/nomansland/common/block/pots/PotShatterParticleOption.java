package com.farcr.nomansland.common.block.pots;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public record PotShatterParticleOption(ResourceLocation model, int persistTicks, List<List<Double>> shapeBoxes, int blockX, int blockY, int blockZ) implements ParticleOptions {

    public static List<List<Double>> extractBoxes(VoxelShape shape) {
        List<List<Double>> boxes = new ArrayList<>();
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                boxes.add(List.of(minX, minY, minZ, maxX, maxY, maxZ)));
        return boxes;
    }

    public PotShatterParticleOption(ResourceLocation model) {
        this(model, 0, List.of(), 0, 0, 0);
    }

    public PotShatterParticleOption(ResourceLocation model, int persistTicks) {
        this(model, persistTicks, List.of(), 0, 0, 0);
    }

    public static MapCodec<PotShatterParticleOption> codec(ParticleType<PotShatterParticleOption> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(PotShatterParticleOption::model),
                Codec.INT.optionalFieldOf("persist_ticks", 0).forGetter(PotShatterParticleOption::persistTicks),
                Codec.DOUBLE.listOf().listOf().optionalFieldOf("shape_boxes", List.of()).forGetter(PotShatterParticleOption::shapeBoxes),
                Codec.INT.optionalFieldOf("block_x", 0).forGetter(PotShatterParticleOption::blockX),
                Codec.INT.optionalFieldOf("block_y", 0).forGetter(PotShatterParticleOption::blockY),
                Codec.INT.optionalFieldOf("block_z", 0).forGetter(PotShatterParticleOption::blockZ)
        ).apply(instance, PotShatterParticleOption::new));
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, PotShatterParticleOption> streamCodec(ParticleType<PotShatterParticleOption> type) {
        return StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, PotShatterParticleOption::model,
                ByteBufCodecs.VAR_INT, PotShatterParticleOption::persistTicks,
                ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list(6)).apply(ByteBufCodecs.list()), PotShatterParticleOption::shapeBoxes,
                ByteBufCodecs.VAR_INT, PotShatterParticleOption::blockX,
                ByteBufCodecs.VAR_INT, PotShatterParticleOption::blockY,
                ByteBufCodecs.VAR_INT, PotShatterParticleOption::blockZ,
                PotShatterParticleOption::new
        );
    }

    @Override
    public ParticleType<?> getType() {
        return NMLParticleTypes.POT_SHATTER.get();
    }
}
