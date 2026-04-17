package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MeetingPointStructure extends Structure {
    public static final MapCodec<MeetingPointStructure> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
            StructureTemplatePool.CODEC.fieldOf("menhir_small_pool").forGetter(s -> s.menhirSmallPool),
            StructureTemplatePool.CODEC.fieldOf("menhir_pool").forGetter(s -> s.menhirPool),
            StructureTemplatePool.CODEC.fieldOf("menhir_large_pool").forGetter(s -> s.menhirLargePool)
        ).apply(instance, MeetingPointStructure::new)
    );

    private static final int PATH_COUNT = 6;
    private static final double PATH_HALF_WIDTH = 6;
    private static final double PATH_WOBBLE_AMPLITUDE = 0.18;
    private static final double PATH_WOBBLE_NOISE_SCALE = 0.04;
    private static final double MIN_DISTANCE_FROM_ALTAR = 15;

    private static final int SLOT_ATTEMPTS = 6000;
    private static final double FIELD_RADIUS = 128.0;
    private static final double DENSITY_FALLOFF_EXPONENT = 2.2;

    private final Holder<StructureTemplatePool> startPool;
    private final Holder<StructureTemplatePool> menhirSmallPool;
    private final Holder<StructureTemplatePool> menhirPool;
    private final Holder<StructureTemplatePool> menhirLargePool;

    public MeetingPointStructure(
            StructureSettings settings,
            Holder<StructureTemplatePool> startPool,
            Holder<StructureTemplatePool> menhirSmallPool,
            Holder<StructureTemplatePool> menhirPool,
            Holder<StructureTemplatePool> menhirLargePool
    ) {
        super(settings);
        this.startPool = startPool;
        this.menhirSmallPool = menhirSmallPool;
        this.menhirPool = menhirPool;
        this.menhirLargePool = menhirLargePool;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        RandomSource random = context.random();
        StructureTemplateManager templates = context.structureTemplateManager();

        int altarX = chunkPos.getMiddleBlockX();
        int altarZ = chunkPos.getMiddleBlockZ();
        int altarY = context.chunkGenerator().getFirstOccupiedHeight(
            altarX, altarZ, Heightmap.Types.WORLD_SURFACE_WG,
            context.heightAccessor(), context.randomState()
        );
        BlockPos altarPos = new BlockPos(altarX, altarY - 1, altarZ);

        ImprovedNoise pathWobbleNoise = new ImprovedNoise(random.fork());
        RandomSource placementRandom = random.fork();
        double pathsBaseAngle = placementRandom.nextDouble() * Math.PI * 2.0;

        return Optional.of(new GenerationStub(altarPos, builder -> {
            List<BoundingBox> placedBoxes = new ArrayList<>();

            StructurePoolElement altarElement = startPool.value().getRandomTemplate(placementRandom);
            BoundingBox altarBox = altarElement.getBoundingBox(templates, altarPos, Rotation.NONE);
            builder.addPiece(new PoolElementStructurePiece(
                templates, altarElement, altarPos, 0, Rotation.NONE, altarBox,
                LiquidSettings.IGNORE_WATERLOGGING
            ));
            placedBoxes.add(altarBox);

            int fieldCenterX = (altarBox.minX() + altarBox.maxX()) / 2;
            int fieldCenterZ = (altarBox.minZ() + altarBox.maxZ()) / 2;

            for (int attempt = 0; attempt < SLOT_ATTEMPTS; attempt++) {
                double angle = placementRandom.nextDouble() * Math.PI * 2.0;
                double normalizedRadius = Math.sqrt(placementRandom.nextDouble());
                double radius = normalizedRadius * FIELD_RADIUS;
                if (radius < MIN_DISTANCE_FROM_ALTAR) continue;

                double acceptance = Math.pow(1.0 - normalizedRadius, DENSITY_FALLOFF_EXPONENT);
                if (placementRandom.nextDouble() > acceptance) continue;

                int menhirX = fieldCenterX + (int) Math.round(Math.cos(angle) * radius);
                int menhirZ = fieldCenterZ + (int) Math.round(Math.sin(angle) * radius);

                double candidateAngle = Math.atan2(menhirZ - fieldCenterZ, menhirX - fieldCenterX);
                if (lyingOnRadialPath(candidateAngle, radius, pathsBaseAngle, pathWobbleNoise)) continue;

                int groundY = context.chunkGenerator().getFirstOccupiedHeight(
                    menhirX, menhirZ, Heightmap.Types.OCEAN_FLOOR_WG,
                    context.heightAccessor(), context.randomState()
                );
                int waterSurfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                    menhirX, menhirZ, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState()
                );
                if (waterSurfaceY > groundY) continue;

                Holder<StructureTemplatePool> selectedPool = pickMenhirPool(placementRandom);
                StructurePoolElement menhirElement = selectedPool.value().getRandomTemplate(placementRandom);

                int directionToCenterX = fieldCenterX - menhirX;
                int directionToCenterZ = fieldCenterZ - menhirZ;
                Rotation rotation = facingRotation(directionToCenterX, directionToCenterZ);

                int menhirBaseY = groundY + 1;

                BlockPos slotAnchor = new BlockPos(menhirX, 0, menhirZ);
                BoundingBox unshiftedBox = menhirElement.getBoundingBox(templates, slotAnchor, rotation);
                int unshiftedCenterX = (unshiftedBox.minX() + unshiftedBox.maxX()) / 2;
                int unshiftedCenterZ = (unshiftedBox.minZ() + unshiftedBox.maxZ()) / 2;
                int centeringShiftX = menhirX - unshiftedCenterX;
                int centeringShiftZ = menhirZ - unshiftedCenterZ;
                BlockPos menhirPos = new BlockPos(menhirX + centeringShiftX, menhirBaseY, menhirZ + centeringShiftZ);
                BoundingBox menhirBox = unshiftedBox.moved(centeringShiftX, menhirBaseY, centeringShiftZ);

                if (overlapsPlaced(menhirBox, placedBoxes)) continue;

                builder.addPiece(new PoolElementStructurePiece(
                    templates, menhirElement, menhirPos, 0, rotation, menhirBox,
                    LiquidSettings.IGNORE_WATERLOGGING
                ));
                placedBoxes.add(menhirBox);
            }
        }));
    }

    private static boolean overlapsPlaced(BoundingBox candidate, List<BoundingBox> placed) {
        int minX = candidate.minX();
        int maxX = candidate.maxX();
        int minZ = candidate.minZ();
        int maxZ = candidate.maxZ();
        for (BoundingBox other : placed) {
            if (maxX >= other.minX() && minX <= other.maxX()
                && maxZ >= other.minZ() && minZ <= other.maxZ()) {
                return true;
            }
        }
        return false;
    }

    private static boolean lyingOnRadialPath(double candidateAngle, double radius, double pathsBaseAngle, ImprovedNoise wobbleNoise) {
        double angleBetweenPaths = (Math.PI * 2.0) / PATH_COUNT;
        for (int pathIndex = 0; pathIndex < PATH_COUNT; pathIndex++) {
            double pathBaseAngle = pathsBaseAngle + pathIndex * angleBetweenPaths;
            double wobble = wobbleNoise.noise(radius * PATH_WOBBLE_NOISE_SCALE, pathIndex * 17.0, 0.0);
            double pathAngleAtRadius = pathBaseAngle + wobble * PATH_WOBBLE_AMPLITUDE;
            double angularOffset = wrappedAngleDifference(candidateAngle, pathAngleAtRadius);
            double tangentialDistance = Math.abs(angularOffset) * radius;
            if (tangentialDistance < PATH_HALF_WIDTH) return true;
        }
        return false;
    }

    private static double wrappedAngleDifference(double a, double b) {
        double difference = (a - b) % (Math.PI * 2.0);
        if (difference > Math.PI) difference -= Math.PI * 2.0;
        if (difference < -Math.PI) difference += Math.PI * 2.0;
        return difference;
    }

    private Holder<StructureTemplatePool> pickMenhirPool(RandomSource random) {
        return switch (random.nextInt(3)) {
            case 0 -> menhirSmallPool;
            case 1 -> menhirPool;
            default -> menhirLargePool;
        };
    }

    private static Rotation facingRotation(int directionToCenterX, int directionToCenterZ) {
        if (Math.abs(directionToCenterX) > Math.abs(directionToCenterZ)) {
            return directionToCenterX > 0 ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        } else {
            return directionToCenterZ >= 0 ? Rotation.NONE : Rotation.CLOCKWISE_180;
        }
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.MEETING_POINT.get();
    }
}
