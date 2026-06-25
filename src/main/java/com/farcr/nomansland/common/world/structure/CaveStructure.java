package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.mixin.JigsawPlacerInvoker;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.farcr.nomansland.common.world.structure.cave.CaveContext;
import com.farcr.nomansland.common.world.structure.cave.CaveEncasing;
import com.farcr.nomansland.common.world.structure.cave.CaveTunnel;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CaveStructure extends EmbeddedStructure {
    public static final MapCodec<CaveStructure> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    Codec.intRange(0, 20).optionalFieldOf("size", 7).forGetter(structure -> structure.maxDepth),
                    Codec.intRange(1, 128).optionalFieldOf("max_distance_from_center", 80).forGetter(structure -> structure.maxDistanceFromCenter),
                    HeightProvider.CODEC.optionalFieldOf("start_height", DEFAULT_START_HEIGHT).forGetter(structure -> structure.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    ExposureTreatment.CODEC.optionalFieldOf("exposure", ExposureTreatment.NONE).forGetter(structure -> structure.exposure),
                    Codec.BOOL.optionalFieldOf("preserve", false).forGetter(EmbeddedStructure::shouldPreserve),
                    Codec.BOOL.optionalFieldOf("tunnel", true).forGetter(structure -> structure.tunnel)
            ).apply(instance, CaveStructure::new)
    );

    private static final int PLACEMENT_ATTEMPTS = 24;
    private static final int CAVE_SEARCH_ABOVE_TARGET = 6;
    private static final int CAVE_SEARCH_BELOW_TARGET = 10;
    private static final int CAVE_CEILING_SEARCH = 14;
    private static final int EMBED_BODY_SAMPLE_DISTANCE = CaveTunnel.TUNNEL_LENGTH + 5;

    private final boolean tunnel;

    public CaveStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxDepth, int maxDistanceFromCenter, HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap, ExposureTreatment exposure, boolean preserve, boolean tunnel) {
        super(settings, startPool, maxDepth, maxDistanceFromCenter, startHeight, projectStartToHeightmap, exposure, preserve);
        this.tunnel = tunnel;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor level = context.heightAccessor();
        RandomState randomState = context.randomState();
        RandomSource random = context.random();
        WorldGenerationContext heightContext = new WorldGenerationContext(generator, level);
        ChunkPos chunkPos = context.chunkPos();

        Map<Long, NoiseColumn> columnCache = new HashMap<>();
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int lowestPlacement = level.getMinBuildHeight() + MIN_HEIGHT_ABOVE_WORLD_BOTTOM;

        for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
            int columnX = chunkMinX + random.nextInt(16);
            int columnZ = chunkMinZ + random.nextInt(16);
            int sampledHeight = this.startHeight.sample(random, heightContext);

            int surfaceHeight = generator.getFirstOccupiedHeight(columnX, columnZ, Heightmap.Types.OCEAN_FLOOR_WG, level, randomState);
            int targetHeight;
            if (this.projectStartToHeightmap.isPresent()) {
                int projectionSurface = this.projectStartToHeightmap.get() == Heightmap.Types.OCEAN_FLOOR_WG
                        ? surfaceHeight
                        : generator.getFirstOccupiedHeight(columnX, columnZ, this.projectStartToHeightmap.get(), level, randomState);
                targetHeight = projectionSurface + sampledHeight;
            } else {
                targetHeight = sampledHeight;
            }

            NoiseColumn column = baseColumn(columnCache, generator, level, randomState, columnX, columnZ);
            int caveFloorY = findCaveFloorNear(column, lowestPlacement, Math.min(targetHeight, surfaceHeight - MIN_DEPTH_BELOW_SURFACE));
            if (caveFloorY == Integer.MIN_VALUE) continue;

            for (Direction directionIntoStone : shuffledHorizontalDirections(random)) {
                BlockPos caveFloor = new BlockPos(columnX, caveFloorY, columnZ);
                if (isSolidEnoughToEmbed(columnCache, generator, level, randomState, caveFloor, directionIntoStone, caveFloorY)) {
                    return assembleStructure(context, directionIntoStone.getOpposite(), caveFloor);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<GenerationStub> assembleStructure(GenerationContext context, Direction directionToCave, BlockPos caveFloor) {
        RegistryAccess registryAccess = context.registryAccess();
        ChunkGenerator generator = context.chunkGenerator();
        StructureTemplateManager templates = context.structureTemplateManager();
        LevelHeightAccessor level = context.heightAccessor();
        RandomState randomState = context.randomState();
        RandomSource random = context.random();
        Registry<StructureTemplatePool> poolRegistry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL);

        Rotation rotation = rotationFacing(directionToCave);
        StructurePoolElement startElement = this.startPool.value().getRandomTemplate(random);
        if (startElement == EmptyPoolElement.INSTANCE) return Optional.empty();

        Direction directionIntoStone = directionToCave.getOpposite();
        int embedGap = this.tunnel ? CaveTunnel.TUNNEL_LENGTH + 1 : 1;
        BlockPos entranceWall = caveFloor.relative(directionIntoStone, embedGap);

        int groundLevelDelta = startElement.getGroundLevelDelta();
        BoundingBox unalignedBounds = startElement.getBoundingBox(templates, entranceWall, rotation);
        PoolElementStructurePiece startPiece = new PoolElementStructurePiece(templates, startElement, entranceWall, groundLevelDelta, rotation, unalignedBounds, LiquidSettings.IGNORE_WATERLOGGING);

        BoundingBox startBounds = startPiece.getBoundingBox();
        int shiftX;
        int shiftZ;
        int shiftY = caveFloor.getY() - (startBounds.minY() + groundLevelDelta);
        if (directionToCave.getAxis() == Direction.Axis.Z) {
            shiftX = entranceWall.getX() - (startBounds.minX() + startBounds.maxX()) / 2;
            shiftZ = directionToCave == Direction.NORTH ? entranceWall.getZ() - startBounds.minZ() : entranceWall.getZ() - startBounds.maxZ();
        } else {
            shiftZ = entranceWall.getZ() - (startBounds.minZ() + startBounds.maxZ()) / 2;
            shiftX = directionToCave == Direction.WEST ? entranceWall.getX() - startBounds.minX() : entranceWall.getX() - startBounds.maxX();
        }
        startPiece.move(shiftX, shiftY, shiftZ);

        BoundingBox alignedBounds = startPiece.getBoundingBox();
        int centerX = (alignedBounds.minX() + alignedBounds.maxX()) / 2;
        int centerY = (alignedBounds.minY() + alignedBounds.maxY()) / 2;
        int centerZ = (alignedBounds.minZ() + alignedBounds.maxZ()) / 2;
        AABB tunnelClearance = this.tunnel ? CaveTunnel.clearanceBox(alignedBounds, groundLevelDelta, directionToCave) : null;

        return Optional.of(new GenerationStub(new BlockPos(centerX, centerY, centerZ), builder -> {
            List<PoolElementStructurePiece> pieces = new ArrayList<>();
            pieces.add(startPiece);

            if (this.maxDepth > 0) {
                AABB placementRegion = new AABB(
                        centerX - this.maxDistanceFromCenter,
                        Math.max(centerY - this.maxDistanceFromCenter, level.getMinBuildHeight() + 1),
                        centerZ - this.maxDistanceFromCenter,
                        centerX + this.maxDistanceFromCenter + 1,
                        Math.min(centerY + this.maxDistanceFromCenter + 1, level.getMaxBuildHeight() - 1),
                        centerZ + this.maxDistanceFromCenter + 1
                );
                VoxelShape freeSpace = Shapes.join(Shapes.create(placementRegion), Shapes.create(AABB.of(alignedBounds)), BooleanOp.ONLY_FIRST);
                if (tunnelClearance != null) {
                    freeSpace = Shapes.join(freeSpace, Shapes.create(tunnelClearance), BooleanOp.ONLY_FIRST);
                }

                JigsawPlacement.Placer placer = new JigsawPlacement.Placer(poolRegistry, this.maxDepth, generator, templates, pieces, random);
                JigsawPlacerInvoker placerInvoker = (JigsawPlacerInvoker) (Object) placer;
                placerInvoker.nml$tryPlacingChildren(startPiece, new MutableObject<>(freeSpace), 0, false, level, randomState, PoolAliasLookup.EMPTY, LiquidSettings.IGNORE_WATERLOGGING);
                while (placer.placing.hasNext()) {
                    JigsawPlacement.PieceState pendingPiece = placer.placing.next();
                    placerInvoker.nml$tryPlacingChildren(pendingPiece.piece(), pendingPiece.free(), pendingPiece.depth(), false, level, randomState, PoolAliasLookup.EMPTY, LiquidSettings.IGNORE_WATERLOGGING);
                }
            }

            pieces.forEach(builder::addPiece);
        }));
    }

    @Override
    protected BoundingBox affectedRegion(PiecesContainer pieces, PoolElementStructurePiece startPiece) {
        if (!this.tunnel) return super.affectedRegion(pieces, startPiece);
        Direction directionToCave = startPiece.getRotation().rotate(Direction.NORTH);
        BlockPos mouth = tunnelMouth(startPiece, directionToCave);
        BlockPos tunnelEnd = mouth.relative(directionToCave, CaveTunnel.TUNNEL_LENGTH + CaveTunnel.TUNNEL_OVERSHOOT);
        int reach = Math.max(CaveEncasing.affectedReach(), CaveTunnel.affectedReach());
        return pieces.calculateBoundingBox().encapsulate(tunnelEnd).inflatedBy(reach);
    }

    @Override
    protected void carveTunnel(CaveContext context, PoolElementStructurePiece startPiece) {
        if (!this.tunnel) return;
        Direction directionToCave = startPiece.getRotation().rotate(Direction.NORTH);
        BoundingBox entranceBounds = startPiece.getBoundingBox();
        int halfWidth = CaveTunnel.tunnelHalfWidth(entranceBounds, directionToCave);
        int halfHeight = CaveTunnel.tunnelHalfHeight(entranceBounds);
        CaveTunnel.carve(context, tunnelMouth(startPiece, directionToCave), directionToCave, halfWidth, halfHeight);
    }

    private static BlockPos tunnelMouth(PoolElementStructurePiece startPiece, Direction directionToCave) {
        BoundingBox entranceBounds = startPiece.getBoundingBox();
        int floorY = entranceBounds.minY() + startPiece.getGroundLevelDelta();
        int centerY = floorY + CaveTunnel.tunnelHalfHeight(entranceBounds);
        int mouthX;
        int mouthZ;
        if (directionToCave.getAxis() == Direction.Axis.Z) {
            mouthX = (entranceBounds.minX() + entranceBounds.maxX()) / 2;
            mouthZ = directionToCave == Direction.NORTH ? entranceBounds.minZ() - 1 : entranceBounds.maxZ() + 1;
        } else {
            mouthZ = (entranceBounds.minZ() + entranceBounds.maxZ()) / 2;
            mouthX = directionToCave == Direction.WEST ? entranceBounds.minX() - 1 : entranceBounds.maxX() + 1;
        }
        return new BlockPos(mouthX, centerY, mouthZ);
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.CAVE.get();
    }

    private static int findCaveFloorNear(NoiseColumn column, int lowestPlacement, int targetHeight) {
        int searchTop = targetHeight + CAVE_SEARCH_ABOVE_TARGET;
        int searchBottom = Math.max(lowestPlacement + 1, targetHeight - CAVE_SEARCH_BELOW_TARGET);
        for (int y = searchTop; y >= searchBottom; y--) {
            if (!column.getBlock(y).isAir() || !column.getBlock(y + 1).isAir()) continue;
            if (!column.getBlock(y - 1).isSolid()) continue;
            for (int above = 2; above <= CAVE_CEILING_SEARCH; above++) {
                if (column.getBlock(y + above).isSolid()) return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private boolean isSolidEnoughToEmbed(Map<Long, NoiseColumn> columnCache, ChunkGenerator generator, LevelHeightAccessor level, RandomState randomState, BlockPos caveFloor, Direction directionIntoStone, int floorY) {
        Direction sideways = directionIntoStone.getClockWise();
        int bodyX = caveFloor.getX() + directionIntoStone.getStepX() * EMBED_BODY_SAMPLE_DISTANCE;
        int bodyZ = caveFloor.getZ() + directionIntoStone.getStepZ() * EMBED_BODY_SAMPLE_DISTANCE;
        int solidSamples = 0;
        int totalSamples = 0;
        for (int along = -3; along <= 3; along += 3) {
            for (int side = -3; side <= 3; side += 3) {
                int sampleX = bodyX + directionIntoStone.getStepX() * along + sideways.getStepX() * side;
                int sampleZ = bodyZ + directionIntoStone.getStepZ() * along + sideways.getStepZ() * side;
                NoiseColumn column = baseColumn(columnCache, generator, level, randomState, sampleX, sampleZ);
                for (int dy = -1; dy <= 5; dy++) {
                    totalSamples++;
                    if (column.getBlock(floorY + dy).isSolid()) solidSamples++;
                }
            }
        }
        return solidSamples >= totalSamples * 0.6;
    }

    private static NoiseColumn baseColumn(Map<Long, NoiseColumn> cache, ChunkGenerator generator, LevelHeightAccessor level, RandomState randomState, int x, int z) {
        long key = ChunkPos.asLong(x, z);
        NoiseColumn cached = cache.get(key);
        if (cached != null) return cached;
        NoiseColumn column = generator.getBaseColumn(x, z, level, randomState);
        cache.put(key, column);
        return column;
    }

    private static Direction[] shuffledHorizontalDirections(RandomSource random) {
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction swap = directions[i];
            directions[i] = directions[j];
            directions[j] = swap;
        }
        return directions;
    }

    private static Rotation rotationFacing(Direction directionToCave) {
        return switch (directionToCave) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }
}
