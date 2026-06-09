package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.mixin.JigsawPlacerInvoker;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CaveStructure extends PreservableStructure {
    public enum ExposureTreatment implements StringRepresentable {
        NONE("none"),
        ERODE("erode"),
        ENCASE("encase");

        public static final Codec<ExposureTreatment> CODEC = StringRepresentable.fromEnum(ExposureTreatment::values);
        private final String serializedName;

        ExposureTreatment(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }

    private static final HeightProvider DEFAULT_START_HEIGHT = UniformHeight.of(VerticalAnchor.absolute(-48), VerticalAnchor.absolute(-8));

    public static final MapCodec<CaveStructure> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    Codec.intRange(0, 20).optionalFieldOf("size", 7).forGetter(structure -> structure.maxDepth),
                    Codec.intRange(1, 128).optionalFieldOf("max_distance_from_center", 80).forGetter(structure -> structure.maxDistanceFromCenter),
                    HeightProvider.CODEC.optionalFieldOf("start_height", DEFAULT_START_HEIGHT).forGetter(structure -> structure.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    ExposureTreatment.CODEC.optionalFieldOf("exposure", ExposureTreatment.NONE).forGetter(structure -> structure.exposureTreatment),
                    Codec.BOOL.optionalFieldOf("preserve", false).forGetter(structure -> structure.preserve)
            ).apply(instance, CaveStructure::new)
    );

    private static final int PLACEMENT_ATTEMPTS = 24;
    private static final int MIN_DEPTH_BELOW_SURFACE = 5;
    private static final int MIN_HEIGHT_ABOVE_WORLD_BOTTOM = 10;
    private static final int LOWEST_CARVE_HEIGHT_ABOVE_WORLD_BOTTOM = 6;

    private static final int CAVE_SEARCH_ABOVE_TARGET = 6;
    private static final int CAVE_SEARCH_BELOW_TARGET = 36;
    private static final int CAVE_CEILING_SEARCH = 14;

    private static final int TUNNEL_LENGTH = 7;
    private static final int TUNNEL_OVERSHOOT = 3;
    private static final int MIN_TUNNEL_HALF = 1;
    private static final int MAX_TUNNEL_HALF = 4;

    private static final double TUNNEL_WANDER = 2.2;
    private static final double TUNNEL_VERTICAL_WANDER = 1.4;
    private static final double TUNNEL_BULGE = 1.3;
    private static final double TUNNEL_ROUGHNESS = 0.34;
    private static final double TUNNEL_MEANDER_FREQ = 0.22;
    private static final double TUNNEL_RADIUS_FREQ = 0.30;
    private static final double TUNNEL_ROUGH_FREQ = 0.45;
    private static final double TUNNEL_SEAL_BAND = 0.55;
    private static final double TUNNEL_MAX_NORMALIZED = 1.0 + TUNNEL_ROUGHNESS + TUNNEL_SEAL_BAND + 0.05;
    private static final int TUNNEL_STEPS_PER_BLOCK = 2;

    private static final double WEATHER_FREQ = 0.14;

    private static final int BOUNDING_BOX_INFLATION = 20;

    private static final double EROSION_REDUCTION = 0.7;
    private static final int EROSION_CENTRALITY_RADIUS = 6;
    private static final double EROSION_PATCH_FREQ = 0.09;
    private static final double EROSION_PATCH_BIAS = 0.35;
    private static final int EROSION_REACH_MAX = 8;
    private static final double EROSION_REACH_BASE = 3.5;
    private static final double EROSION_TOP_BIAS = 1.0;
    private static final double EROSION_BOTTOM_BIAS = 0.2;
    private static final double EROSION_CEILING_FACTOR = 0.55;
    private static final double EROSION_FACE_WEIGHT = 0.7;
    private static final double EROSION_CENTRE_STRENGTH = 0.45;
    private static final double EROSION_WOBBLE_FREQ = 0.3;
    private static final double EROSION_WOBBLE = 0.3;
    private static final int EROSION_SAMPLE_RADIUS = 2;
    private static final double EROSION_CONVEXITY = 2.0;
    private static final double EROSION_EXPOSURE_BASELINE = 0.3;
    private static final double EROSION_DETAIL_FREQ = 0.6;
    private static final double EROSION_DETAIL = 0.28;

    private static final int ENCASE_SIDE_RADIUS = 2;
    private static final int ENCASE_ABOVE_RADIUS = 3;
    private static final int ENCASE_BELOW_RADIUS = 2;
    private static final double ENCASE_EDGE_NOISE = 0.35;
    private static final int POCKET_MIN_DEPTH = 2;
    private static final int POCKET_MAX_DEPTH = 5;

    private static final NormalNoise MEANDER_NOISE = NormalNoise.create(RandomSource.create(48271L), -1, 1.0, 0.5);
    private static final NormalNoise WEATHER_NOISE = NormalNoise.create(RandomSource.create(95791L), -2, 1.0, 1.0, 0.6);

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final int maxDistanceFromCenter;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final ExposureTreatment exposureTreatment;
    private final boolean preserve;

    public CaveStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxDepth, int maxDistanceFromCenter, HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap, ExposureTreatment exposureTreatment, boolean preserve) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.exposureTreatment = exposureTreatment;
        this.preserve = preserve;
    }

    @Override
    protected boolean shouldPreserve() {
        return this.preserve;
    }

    @Override
    public BoundingBox adjustBoundingBox(BoundingBox boundingBox) {
        return boundingBox.inflatedBy(BOUNDING_BOX_INFLATION);
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
        BlockPos entranceWall = caveFloor.relative(directionIntoStone, TUNNEL_LENGTH + 1);

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
        AABB tunnelClearance = tunnelClearanceBox(alignedBounds, groundLevelDelta, directionToCave);

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
                freeSpace = Shapes.join(freeSpace, Shapes.create(tunnelClearance), BooleanOp.ONLY_FIRST);

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
    protected void afterPlaceStructure(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBounds, ChunkPos chunkPos, PiecesContainer pieces) {
        if (pieces.pieces().isEmpty()) return;
        if (!(pieces.pieces().get(0) instanceof PoolElementStructurePiece startPiece)) return;

        BoundingBox entranceBounds = startPiece.getBoundingBox();
        Direction directionToCave = startPiece.getRotation().rotate(Direction.NORTH);

        int floorY = entranceBounds.minY() + startPiece.getGroundLevelDelta();
        int tunnelHalfWidth = tunnelHalfWidth(entranceBounds, directionToCave);
        int tunnelHalfHeight = tunnelHalfHeight(entranceBounds);
        int tunnelCenterY = floorY + tunnelHalfHeight;

        int mouthX;
        int mouthZ;
        if (directionToCave.getAxis() == Direction.Axis.Z) {
            mouthX = (entranceBounds.minX() + entranceBounds.maxX()) / 2;
            mouthZ = directionToCave == Direction.NORTH ? entranceBounds.minZ() - 1 : entranceBounds.maxZ() + 1;
        } else {
            mouthZ = (entranceBounds.minZ() + entranceBounds.maxZ()) / 2;
            mouthX = directionToCave == Direction.WEST ? entranceBounds.minX() - 1 : entranceBounds.maxX() + 1;
        }
        BlockPos tunnelMouth = new BlockPos(mouthX, tunnelCenterY, mouthZ);

        BlockPos tunnelEnd = tunnelMouth.relative(directionToCave, TUNNEL_LENGTH + TUNNEL_OVERSHOOT);
        int tunnelReach = MAX_TUNNEL_HALF + 2 + (int) Math.ceil(TUNNEL_WANDER + TUNNEL_BULGE);
        int postProcessReach = Math.max(tunnelReach, Math.max(ENCASE_ABOVE_RADIUS + 1, POCKET_MAX_DEPTH + 1));
        BoundingBox affectedRegion = pieces.calculateBoundingBox()
                .encapsulate(tunnelEnd)
                .inflatedBy(postProcessReach);
        if (!chunkBounds.intersects(affectedRegion)) return;

        Occupancy occupancy = new Occupancy(pieces);
        int lowestY = lowestCarveY(level);

        if (this.exposureTreatment == ExposureTreatment.ENCASE) {
            List<Hole> wallHoles = detectWallHoles(level, chunkBounds, occupancy, lowestY);
            LongOpenHashSet pocketAir = new LongOpenHashSet();
            carveHolePockets(level, chunkBounds, wallHoles, occupancy, lowestY, pocketAir);
            encaseExposedFaces(level, chunkBounds, occupancy, lowestY, pocketAir);
        }
        carveTunnel(level, chunkBounds, tunnelMouth, directionToCave, tunnelHalfWidth, tunnelHalfHeight, occupancy, lowestY);
        if (this.exposureTreatment == ExposureTreatment.ERODE) {
            erodeExposedFaces(level, chunkBounds, occupancy, lowestY);
        }
    }

    private void carveTunnel(WorldGenLevel level, BoundingBox chunkBounds, BlockPos mouth, Direction directionToCave, int baseHalfWidth, int baseHalfHeight, Occupancy occupancy, int lowestY) {
        Direction sideways = directionToCave.getClockWise();
        double forwardX = directionToCave.getStepX();
        double forwardZ = directionToCave.getStepZ();
        double sideX = sideways.getStepX();
        double sideZ = sideways.getStepZ();

        double originX = mouth.getX() + 0.5;
        double originY = mouth.getY() + 0.5;
        double originZ = mouth.getZ() + 0.5;

        int length = TUNNEL_LENGTH + TUNNEL_OVERSHOOT;
        int samples = length * TUNNEL_STEPS_PER_BLOCK;
        double phaseA = mouth.getX() * 0.37 + mouth.getZ() * 0.11;
        double phaseB = mouth.getZ() * 0.41 - mouth.getX() * 0.13;

        int spanX = chunkBounds.getXSpan();
        int spanZ = chunkBounds.getZSpan();
        int[] columnTop = new int[spanX * spanZ];
        int[] columnBottom = new int[spanX * spanZ];
        Arrays.fill(columnTop, Integer.MIN_VALUE);
        Arrays.fill(columnBottom, Integer.MAX_VALUE);

        for (int i = 0; i <= samples; i++) {
            double progress = (double) i / samples;
            double forward = progress * length;
            double envelope = Math.sin(Math.PI * progress);

            double lateral = signedNoise(forward * TUNNEL_MEANDER_FREQ, phaseA, phaseB) * TUNNEL_WANDER * envelope;
            double vertical = signedNoise(forward * TUNNEL_MEANDER_FREQ + 53.7, phaseA + 17.3, phaseB - 9.1) * TUNNEL_VERTICAL_WANDER * envelope;
            double radiusNoise = signedNoise(forward * TUNNEL_RADIUS_FREQ + 101.5, phaseB + 4.2, phaseA - 6.6);

            double halfWidth = Math.clamp(baseHalfWidth + 0.6 + envelope * TUNNEL_BULGE + radiusNoise * 0.9, 1.4, MAX_TUNNEL_HALF + 1.6);
            double halfHeight = Math.clamp(baseHalfHeight + 0.4 + envelope * TUNNEL_BULGE * 0.7 + radiusNoise * 0.6, 1.2, MAX_TUNNEL_HALF + 1.0);

            double centerX = originX + forwardX * forward + sideX * lateral;
            double centerY = originY + vertical;
            double centerZ = originZ + forwardZ * forward + sideZ * lateral;

            accumulateTunnelBlob(level, chunkBounds, centerX, centerY, centerZ, halfWidth, halfHeight, occupancy, lowestY, columnTop, columnBottom, spanZ);
        }

        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < spanX; localX++) {
            for (int localZ = 0; localZ < spanZ; localZ++) {
                int index = localX * spanZ + localZ;
                int top = columnTop[index];
                if (top == Integer.MIN_VALUE) continue;
                carveTunnelColumn(level, chunkBounds.minX() + localX, chunkBounds.minZ() + localZ, top, columnBottom[index], occupancy, lowestY, position);
            }
        }
    }

    private void accumulateTunnelBlob(WorldGenLevel level, BoundingBox chunkBounds, double centerX, double centerY, double centerZ, double halfWidth, double halfHeight, Occupancy occupancy, int lowestY, int[] columnTop, int[] columnBottom, int spanZ) {
        int minX = Math.max(chunkBounds.minX(), Mth.floor(centerX - halfWidth) - 1);
        int maxX = Math.min(chunkBounds.maxX(), Mth.floor(centerX + halfWidth) + 1);
        int minY = Mth.floor(centerY - halfHeight) - 1;
        int maxY = Mth.floor(centerY + halfHeight) + 1;
        int minZ = Math.max(chunkBounds.minZ(), Mth.floor(centerZ - halfWidth) - 1);
        int maxZ = Math.min(chunkBounds.maxZ(), Mth.floor(centerZ + halfWidth) + 1);
        double invWidth = 1.0 / (halfWidth + 0.5);
        double invHeight = 1.0 / (halfHeight + 0.5);

        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            double dx = (x + 0.5 - centerX) * invWidth;
            double dx2 = dx * dx;
            int columnBase = (x - chunkBounds.minX()) * spanZ - chunkBounds.minZ();
            for (int z = minZ; z <= maxZ; z++) {
                double dz = (z + 0.5 - centerZ) * invWidth;
                double horizontal = dx2 + dz * dz;
                int index = columnBase + z;
                for (int y = minY; y <= maxY; y++) {
                    double dy = (y + 0.5 - centerY) * invHeight;
                    double distance = horizontal + dy * dy;
                    if (distance > TUNNEL_MAX_NORMALIZED) continue;
                    double rough = (noise01(x * TUNNEL_ROUGH_FREQ, y * TUNNEL_ROUGH_FREQ, z * TUNNEL_ROUGH_FREQ) - 0.45) * TUNNEL_ROUGHNESS;
                    double edge = 1.0 + rough;
                    if (distance <= edge) {
                        if (y > columnTop[index]) columnTop[index] = y;
                        if (y < columnBottom[index]) columnBottom[index] = y;
                    } else if (distance <= edge + TUNNEL_SEAL_BAND) {
                        position.set(x, y, z);
                        sealLeakingBlock(level, chunkBounds, position, occupancy, lowestY);
                    }
                }
            }
        }
    }

    private void carveTunnelColumn(WorldGenLevel level, int x, int z, int top, int bottom, Occupancy occupancy, int lowestY, BlockPos.MutableBlockPos position) {
        for (int y = top; y >= bottom; y--) {
            position.set(x, y, z);
            BlockState state = level.getBlockState(position);
            if (state.isAir()) continue;
            if (isCarvableTerrain(state, position, occupancy, lowestY)) {
                level.setBlock(position, Blocks.AIR.defaultBlockState(), 2);
            } else {
                return;
            }
        }
    }

    private boolean isCarvableTerrain(BlockState state, BlockPos position, Occupancy occupancy, int lowestY) {
        return position.getY() >= lowestY
                && !occupancy.contains(position)
                && state.is(BlockTags.OVERWORLD_CARVER_REPLACEABLES);
    }

    private void sealLeakingBlock(WorldGenLevel level, BoundingBox chunkBounds, BlockPos position, Occupancy occupancy, int lowestY) {
        if (!chunkBounds.isInside(position) || position.getY() < lowestY) return;
        if (occupancy.contains(position)) return;
        BlockState state = level.getBlockState(position);
        boolean wouldLeak = state.getBlock() instanceof FallingBlock || !state.getFluidState().isEmpty();
        if (wouldLeak) {
            level.setBlock(position, surroundingStone(position), 2);
        }
    }

    private void erodeExposedFaces(WorldGenLevel level, BoundingBox chunkBounds, Occupancy occupancy, int lowestY) {
        BoundingBox structureBounds = occupancy.total();
        int coreMinX = Math.max(chunkBounds.minX(), structureBounds.minX());
        int coreMaxX = Math.min(chunkBounds.maxX(), structureBounds.maxX());
        int coreMinZ = Math.max(chunkBounds.minZ(), structureBounds.minZ());
        int coreMaxZ = Math.min(chunkBounds.maxZ(), structureBounds.maxZ());
        if (coreMinX > coreMaxX || coreMinZ > coreMaxZ) return;

        int reach = EROSION_REACH_MAX;
        int scanMinX = coreMinX - reach;
        int scanMaxX = coreMaxX + reach;
        int scanMinZ = coreMinZ - reach;
        int scanMaxZ = coreMaxZ + reach;
        int scanMinY = Math.max(structureBounds.minY() - reach, level.getMinBuildHeight());
        int scanMaxY = Math.min(structureBounds.maxY() + reach, level.getMaxBuildHeight() - 1);
        int sizeX = scanMaxX - scanMinX + 1;
        int sizeY = scanMaxY - scanMinY + 1;
        int sizeZ = scanMaxZ - scanMinZ + 1;

        boolean[] exteriorAir = new boolean[sizeX * sizeY * sizeZ];
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = scanMinX; x <= scanMaxX; x++) {
            for (int y = scanMinY; y <= scanMaxY; y++) {
                for (int z = scanMinZ; z <= scanMaxZ; z++) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).isAir() && !occupancy.contains(cursor)) {
                        exteriorAir[((x - scanMinX) * sizeY + (y - scanMinY)) * sizeZ + (z - scanMinZ)] = true;
                    }
                }
            }
        }

        int exposedMinY = Math.max(scanMinY, structureBounds.minY());
        int exposedMaxY = Math.min(scanMaxY, structureBounds.maxY());
        boolean[] exposedColumn = new boolean[sizeX * sizeZ];
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (int x = scanMinX; x <= scanMaxX; x++) {
            for (int z = scanMinZ; z <= scanMaxZ; z++) {
                boolean exposed = false;
                for (int y = exposedMaxY; y >= exposedMinY && !exposed; y--) {
                    cursor.set(x, y, z);
                    if (!occupancy.contains(cursor)) continue;
                    if (!level.getBlockState(cursor).is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) continue;
                    if (touchesExteriorAir(level, cursor, occupancy, neighbour)) exposed = true;
                }
                exposedColumn[(x - scanMinX) * sizeZ + (z - scanMinZ)] = exposed;
            }
        }

        int structureSpanY = Math.max(1, structureBounds.maxY() - structureBounds.minY());
        int erodeTopY = structureBounds.maxY();
        int erodeBottomY = Math.max(lowestY, structureBounds.minY());
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
        for (int x = coreMinX; x <= coreMaxX; x++) {
            for (int z = coreMinZ; z <= coreMaxZ; z++) {
                double centrality = distanceToUnexposed(exposedColumn, scanMinX, scanMinZ, sizeX, sizeZ, x, z);
                double centerFactor = Math.clamp(centrality / EROSION_CENTRALITY_RADIUS, 0.0, 1.0);
                centerFactor = centerFactor * centerFactor * (3.0 - 2.0 * centerFactor);
                if (centerFactor <= 0.0) continue;
                double patch = 1.0 + (noise01(x * EROSION_PATCH_FREQ, 7.0, z * EROSION_PATCH_FREQ) - 0.5) * 2.0 * EROSION_PATCH_BIAS;

                for (int y = erodeTopY; y >= erodeBottomY; y--) {
                    position.set(x, y, z);
                    if (!occupancy.contains(position)) continue;
                    if (!level.getBlockState(position).is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) continue;
                    below.set(x, y - 1, z);
                    if (!occupancy.contains(below)) continue;
                    if (supportsNeighborAttachment(level, position, neighbour)) continue;

                    double heightFraction = Math.clamp((double) (y - structureBounds.minY()) / structureSpanY, 0.0, 1.0);
                    double heightBias = EROSION_BOTTOM_BIAS + (EROSION_TOP_BIAS - EROSION_BOTTOM_BIAS) * heightFraction;
                    double wobble = 1.0
                            + (noise01(x * EROSION_WOBBLE_FREQ, y * EROSION_WOBBLE_FREQ, z * EROSION_WOBBLE_FREQ) - 0.5) * 2.0 * EROSION_WOBBLE
                            + (noise01(x * EROSION_DETAIL_FREQ, y * EROSION_DETAIL_FREQ, z * EROSION_DETAIL_FREQ) - 0.5) * 2.0 * EROSION_DETAIL;
                    double exposure = exteriorAirFraction(exteriorAir, scanMinX, scanMinY, scanMinZ, sizeX, sizeY, sizeZ, x, y, z, EROSION_SAMPLE_RADIUS);
                    double convexBoost = EROSION_CONVEXITY * Math.max(0.0, exposure - EROSION_EXPOSURE_BASELINE);
                    double weight = EROSION_FACE_WEIGHT + EROSION_CENTRE_STRENGTH * centerFactor + convexBoost;
                    int aboveIndexY = (y + 1) - scanMinY;
                    boolean exposedFromAbove = aboveIndexY < sizeY
                            && exteriorAir[((x - scanMinX) * sizeY + aboveIndexY) * sizeZ + (z - scanMinZ)];
                    double ceilingFactor = exposedFromAbove ? EROSION_CEILING_FACTOR : 1.0;
                    double reachHere = Math.min(EROSION_REACH_MAX, EROSION_REACH_BASE * heightBias * patch * wobble * EROSION_REDUCTION * weight * ceilingFactor);
                    if (reachHere < 1.0) continue;

                    if (exteriorAirWithin(exteriorAir, scanMinX, scanMinY, scanMinZ, sizeX, sizeY, sizeZ, x, y, z, reachHere)) {
                        level.setBlock(position, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
        pruneFloatingCeiling(level, occupancy, exteriorAir, scanMinX, scanMinY, scanMinZ, sizeX, sizeY, sizeZ, coreMinX, coreMaxX, coreMinZ, coreMaxZ, erodeBottomY, erodeTopY);
    }

    private static final int[][] CEILING_NEIGHBORS = ceilingNeighbors();

    private static int[][] ceilingNeighbors() {
        List<int[]> list = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    list.add(new int[]{dx, dy, dz});
                }
            }
        }
        return list.toArray(new int[0][]);
    }

    private void pruneFloatingCeiling(WorldGenLevel level, Occupancy occupancy, boolean[] exteriorAir,
                                      int scanMinX, int scanMinY, int scanMinZ, int sizeX, int sizeY, int sizeZ,
                                      int coreMinX, int coreMaxX, int coreMinZ, int coreMaxZ, int erodeBottomY, int erodeTopY) {
        int scanMaxX = scanMinX + sizeX - 1;
        int scanMaxZ = scanMinZ + sizeZ - 1;
        LongOpenHashSet ceilingRemnants = new LongOpenHashSet();
        LongOpenHashSet buriedCeiling = new LongOpenHashSet();
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos vertical = new BlockPos.MutableBlockPos();
        for (int x = scanMinX; x <= scanMaxX; x++) {
            for (int z = scanMinZ; z <= scanMaxZ; z++) {
                for (int y = erodeBottomY; y <= erodeTopY; y++) {
                    int aboveIndexY = (y + 1) - scanMinY;
                    if (aboveIndexY < 0 || aboveIndexY >= sizeY) continue;
                    position.set(x, y, z);
                    if (!occupancy.contains(position)) continue;
                    if (!level.getBlockState(position).is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) continue;
                    vertical.set(x, y - 1, z);
                    if (!level.getBlockState(vertical).isAir()) continue;
                    boolean aboveExteriorAir = exteriorAir[((x - scanMinX) * sizeY + aboveIndexY) * sizeZ + (z - scanMinZ)];
                    if (aboveExteriorAir) {
                        ceilingRemnants.add(BlockPos.asLong(x, y, z));
                    } else {
                        vertical.set(x, y + 1, z);
                        if (!occupancy.contains(vertical)) buriedCeiling.add(BlockPos.asLong(x, y, z));
                    }
                }
            }
        }
        if (ceilingRemnants.isEmpty()) return;

        ArrayDeque<Long> queue = new ArrayDeque<>();
        LongOpenHashSet connected = new LongOpenHashSet();
        for (long cell : ceilingRemnants) {
            if (touchesCeilingAnchor(cell, buriedCeiling) && connected.add(cell)) queue.add(cell);
        }
        while (!queue.isEmpty()) {
            long cell = queue.poll();
            int cx = BlockPos.getX(cell);
            int cy = BlockPos.getY(cell);
            int cz = BlockPos.getZ(cell);
            for (int[] offset : CEILING_NEIGHBORS) {
                long neighbour = BlockPos.asLong(cx + offset[0], cy + offset[1], cz + offset[2]);
                if (ceilingRemnants.contains(neighbour) && connected.add(neighbour)) queue.add(neighbour);
            }
        }

        BlockPos.MutableBlockPos neighbourPos = new BlockPos.MutableBlockPos();
        for (long cell : ceilingRemnants) {
            if (connected.contains(cell)) continue;
            int cx = BlockPos.getX(cell);
            int cy = BlockPos.getY(cell);
            int cz = BlockPos.getZ(cell);
            if (cx < coreMinX || cx > coreMaxX || cz < coreMinZ || cz > coreMaxZ) continue;
            position.set(cx, cy, cz);
            if (supportsNeighborAttachment(level, position, neighbourPos)) continue;
            level.setBlock(position, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    private static boolean touchesCeilingAnchor(long cell, LongOpenHashSet buriedCeiling) {
        int cx = BlockPos.getX(cell);
        int cy = BlockPos.getY(cell);
        int cz = BlockPos.getZ(cell);
        for (int[] offset : CEILING_NEIGHBORS) {
            if (buriedCeiling.contains(BlockPos.asLong(cx + offset[0], cy + offset[1], cz + offset[2]))) return true;
        }
        return false;
    }

    private static boolean exteriorAirWithin(boolean[] exteriorAir, int scanMinX, int scanMinY, int scanMinZ, int sizeX, int sizeY, int sizeZ, int x, int y, int z, double reach) {
        int radius = Mth.ceil(reach);
        double reachSquared = reach * reach;
        for (int dx = -radius; dx <= radius; dx++) {
            int localX = x + dx - scanMinX;
            if (localX < 0 || localX >= sizeX) continue;
            int dx2 = dx * dx;
            for (int dy = -radius; dy <= radius; dy++) {
                int localY = y + dy - scanMinY;
                if (localY < 0 || localY >= sizeY) continue;
                int planeBase = (localX * sizeY + localY) * sizeZ;
                int dxy2 = dx2 + dy * dy;
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dxy2 + dz * dz > reachSquared) continue;
                    int localZ = z + dz - scanMinZ;
                    if (localZ < 0 || localZ >= sizeZ) continue;
                    if (exteriorAir[planeBase + localZ]) return true;
                }
            }
        }
        return false;
    }

    private static double distanceToUnexposed(boolean[] exposed, int scanMinX, int scanMinZ, int sizeX, int sizeZ, int x, int z) {
        int nearest = EROSION_CENTRALITY_RADIUS * EROSION_CENTRALITY_RADIUS + 1;
        for (int dx = -EROSION_CENTRALITY_RADIUS; dx <= EROSION_CENTRALITY_RADIUS; dx++) {
            for (int dz = -EROSION_CENTRALITY_RADIUS; dz <= EROSION_CENTRALITY_RADIUS; dz++) {
                int distanceSquared = dx * dx + dz * dz;
                if (distanceSquared >= nearest) continue;
                int localX = x + dx - scanMinX;
                int localZ = z + dz - scanMinZ;
                boolean wall = localX < 0 || localX >= sizeX || localZ < 0 || localZ >= sizeZ
                        || !exposed[localX * sizeZ + localZ];
                if (wall) nearest = distanceSquared;
            }
        }
        return Math.sqrt(nearest);
    }

    private static double exteriorAirFraction(boolean[] exteriorAir, int scanMinX, int scanMinY, int scanMinZ, int sizeX, int sizeY, int sizeZ, int x, int y, int z, int radius) {
        int air = 0;
        int total = 0;
        double radiusSquared = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            int localX = x + dx - scanMinX;
            int dx2 = dx * dx;
            for (int dy = -radius; dy <= radius; dy++) {
                int localY = y + dy - scanMinY;
                int dxy2 = dx2 + dy * dy;
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dxy2 + dz * dz > radiusSquared) continue;
                    total++;
                    int localZ = z + dz - scanMinZ;
                    if (localX < 0 || localX >= sizeX || localY < 0 || localY >= sizeY || localZ < 0 || localZ >= sizeZ) continue;
                    if (exteriorAir[(localX * sizeY + localY) * sizeZ + localZ]) air++;
                }
            }
        }
        return total == 0 ? 0.0 : (double) air / total;
    }

    private boolean supportsNeighborAttachment(WorldGenLevel level, BlockPos position, BlockPos.MutableBlockPos neighbour) {
        for (Direction direction : Direction.values()) {
            neighbour.set(position.getX() + direction.getStepX(), position.getY() + direction.getStepY(), position.getZ() + direction.getStepZ());
            BlockState neighbourState = level.getBlockState(neighbour);
            if (neighbourState.isAir() || !neighbourState.getFluidState().isEmpty()) continue;
            if (neighbourState.isCollisionShapeFullBlock(level, neighbour)) continue;
            return true;
        }
        return false;
    }

    private record Hole(BlockPos position, Direction outward) {
    }

    private List<Hole> detectWallHoles(WorldGenLevel level, BoundingBox chunkBounds, Occupancy occupancy, int lowestY) {
        List<Hole> holes = new ArrayList<>();
        BlockPos.MutableBlockPos interior = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (BoundingBox bounds : occupancy.rooms()) {
            int minX = Math.max(chunkBounds.minX() - POCKET_MAX_DEPTH, bounds.minX());
            int maxX = Math.min(chunkBounds.maxX() + POCKET_MAX_DEPTH, bounds.maxX());
            int minY = Math.max(Math.max(chunkBounds.minY() - POCKET_MAX_DEPTH, bounds.minY()), lowestY);
            int maxY = Math.min(chunkBounds.maxY() + POCKET_MAX_DEPTH, bounds.maxY());
            int minZ = Math.max(chunkBounds.minZ() - POCKET_MAX_DEPTH, bounds.minZ());
            int maxZ = Math.min(chunkBounds.maxZ() + POCKET_MAX_DEPTH, bounds.maxZ());
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        interior.set(x, y, z);
                        if (!level.getBlockState(interior).isAir() || !occupancy.contains(interior)) continue;
                        for (Direction direction : Direction.values()) {
                            neighbour.set(x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ());
                            if (level.getBlockState(neighbour).isAir() && !occupancy.contains(neighbour)) {
                                holes.add(new Hole(interior.immutable(), direction));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return holes;
    }

    private void carveHolePockets(WorldGenLevel level, BoundingBox chunkBounds, List<Hole> holes, Occupancy occupancy, int lowestY, LongOpenHashSet pocketAir) {
        BlockPos.MutableBlockPos center = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        for (Hole hole : holes) {
            RandomSource holeRandom = RandomSource.create((long) hole.position().getX() * 7919L ^ (long) hole.position().getY() * 104729L ^ (long) hole.position().getZ() * 1299721L);
            int depth = POCKET_MIN_DEPTH + holeRandom.nextInt(POCKET_MAX_DEPTH - POCKET_MIN_DEPTH + 1);
            Direction outward = hole.outward();
            center.set(hole.position());
            for (int step = 1; step <= depth; step++) {
                center.move(outward);
                int radius = step < depth ? 1 : 0;
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            int manhattan = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                            boolean carveThis = manhattan <= 1 || noise01((center.getX() + dx) * WEATHER_FREQ, (center.getY() + dy) * WEATHER_FREQ, (center.getZ() + dz) * WEATHER_FREQ) <= 0.45;
                            if (!carveThis) continue;
                            cell.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                            carvePocketCell(level, chunkBounds, cell, above, occupancy, lowestY, pocketAir);
                        }
                    }
                }
            }
        }
    }

    private void carvePocketCell(WorldGenLevel level, BoundingBox chunkBounds, BlockPos cell, BlockPos.MutableBlockPos above, Occupancy occupancy, int lowestY, LongOpenHashSet pocketAir) {
        if (!chunkBounds.isInside(cell) || cell.getY() < lowestY) return;
        if (occupancy.contains(cell)) return;
        if (!level.getBlockState(cell).is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) return;
        above.set(cell.getX(), cell.getY() + 1, cell.getZ());
        BlockState aboveState = level.getBlockState(above);
        if (!aboveState.isAir() && !pocketAir.contains(above.asLong()) && !isCarvableTerrain(aboveState, above, occupancy, lowestY)) return;
        level.setBlock(cell, Blocks.AIR.defaultBlockState(), 2);
        pocketAir.add(cell.asLong());
    }

    private void encaseExposedFaces(WorldGenLevel level, BoundingBox chunkBounds, Occupancy occupancy, int lowestY, LongOpenHashSet pocketAir) {
        int reach = Math.max(ENCASE_SIDE_RADIUS, ENCASE_ABOVE_RADIUS);

        List<BlockPos> exposedFaces = new ArrayList<>();
        BlockPos.MutableBlockPos face = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (BoundingBox pieceBounds : occupancy.rooms()) {
            int minX = Math.max(chunkBounds.minX() - reach, pieceBounds.minX());
            int maxX = Math.min(chunkBounds.maxX() + reach, pieceBounds.maxX());
            int minY = Math.max(Math.max(chunkBounds.minY() - reach, pieceBounds.minY()), lowestY);
            int maxY = Math.min(chunkBounds.maxY() + reach, pieceBounds.maxY());
            int minZ = Math.max(chunkBounds.minZ() - reach, pieceBounds.minZ());
            int maxZ = Math.min(chunkBounds.maxZ() + reach, pieceBounds.maxZ());
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        face.set(x, y, z);
                        if (level.getBlockState(face).isAir()) continue;
                        if (touchesExteriorAir(level, face, occupancy, neighbour)) exposedFaces.add(face.immutable());
                    }
                }
            }
        }

        BlockPos.MutableBlockPos filled = new BlockPos.MutableBlockPos();
        for (BlockPos faceBlock : exposedFaces) {
            for (int offsetX = -ENCASE_SIDE_RADIUS; offsetX <= ENCASE_SIDE_RADIUS; offsetX++) {
                for (int offsetZ = -ENCASE_SIDE_RADIUS; offsetZ <= ENCASE_SIDE_RADIUS; offsetZ++) {
                    for (int offsetY = -ENCASE_BELOW_RADIUS; offsetY <= ENCASE_ABOVE_RADIUS; offsetY++) {
                        filled.set(faceBlock.getX() + offsetX, faceBlock.getY() + offsetY, faceBlock.getZ() + offsetZ);
                        if (!chunkBounds.isInside(filled) || filled.getY() < lowestY) continue;
                        if (!level.getBlockState(filled).isAir() || occupancy.contains(filled)) continue;
                        if (leadsToOpenPocket(filled, pocketAir, neighbour)) continue;
                        double normalizedX = (double) offsetX / ENCASE_SIDE_RADIUS;
                        double normalizedZ = (double) offsetZ / ENCASE_SIDE_RADIUS;
                        double normalizedY = offsetY >= 0 ? (double) offsetY / ENCASE_ABOVE_RADIUS : (double) offsetY / ENCASE_BELOW_RADIUS;
                        double distance = normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ;
                        double threshold = 1.0 - noise01(filled.getX() * WEATHER_FREQ, filled.getY() * WEATHER_FREQ, filled.getZ() * WEATHER_FREQ) * ENCASE_EDGE_NOISE;
                        if (distance <= threshold) {
                            level.setBlock(filled, surroundingStone(filled), 2);
                        }
                    }
                }
            }
        }
    }

    private boolean touchesExteriorAir(WorldGenLevel level, BlockPos position, Occupancy occupancy, BlockPos.MutableBlockPos neighbour) {
        for (Direction direction : Direction.values()) {
            neighbour.set(position.getX() + direction.getStepX(), position.getY() + direction.getStepY(), position.getZ() + direction.getStepZ());
            if (level.getBlockState(neighbour).isAir() && !occupancy.contains(neighbour)) return true;
        }
        return false;
    }

    private boolean leadsToOpenPocket(BlockPos position, LongOpenHashSet pocketAir, BlockPos.MutableBlockPos neighbour) {
        if (pocketAir.isEmpty()) return false;
        if (pocketAir.contains(position.asLong())) return true;
        for (Direction direction : Direction.values()) {
            neighbour.set(position.getX() + direction.getStepX(), position.getY() + direction.getStepY(), position.getZ() + direction.getStepZ());
            if (pocketAir.contains(neighbour.asLong())) return true;
        }
        return false;
    }

    private static final class Occupancy {
        private static final int CELL_SHIFT = 3;
        private final List<BoundingBox> rooms;
        private final BoundingBox total;
        private final LongOpenHashSet cells = new LongOpenHashSet();

        private Occupancy(PiecesContainer pieces) {
            List<BoundingBox> list = new ArrayList<>();
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (StructurePiece piece : pieces.pieces()) {
                BoundingBox bounds = piece.getBoundingBox();
                list.add(bounds);
                minX = Math.min(minX, bounds.minX());
                minY = Math.min(minY, bounds.minY());
                minZ = Math.min(minZ, bounds.minZ());
                maxX = Math.max(maxX, bounds.maxX());
                maxY = Math.max(maxY, bounds.maxY());
                maxZ = Math.max(maxZ, bounds.maxZ());
                for (int cellX = bounds.minX() >> CELL_SHIFT; cellX <= bounds.maxX() >> CELL_SHIFT; cellX++) {
                    for (int cellY = bounds.minY() >> CELL_SHIFT; cellY <= bounds.maxY() >> CELL_SHIFT; cellY++) {
                        for (int cellZ = bounds.minZ() >> CELL_SHIFT; cellZ <= bounds.maxZ() >> CELL_SHIFT; cellZ++) {
                            this.cells.add(BlockPos.asLong(cellX, cellY, cellZ));
                        }
                    }
                }
            }
            this.rooms = list;
            this.total = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        private boolean contains(BlockPos position) {
            if (!this.total.isInside(position)) return false;
            long key = BlockPos.asLong(position.getX() >> CELL_SHIFT, position.getY() >> CELL_SHIFT, position.getZ() >> CELL_SHIFT);
            if (!this.cells.contains(key)) return false;
            for (BoundingBox bounds : this.rooms) {
                if (bounds.isInside(position)) return true;
            }
            return false;
        }

        private List<BoundingBox> rooms() {
            return this.rooms;
        }

        private BoundingBox total() {
            return this.total;
        }
    }

    private int lowestCarveY(WorldGenLevel level) {
        return level.getMinBuildHeight() + LOWEST_CARVE_HEIGHT_ABOVE_WORLD_BOTTOM;
    }

    private BlockState surroundingStone(BlockPos position) {
        return position.getY() <= 0 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
    }

    private AABB tunnelClearanceBox(BoundingBox structureBounds, int groundLevelDelta, Direction directionToCave) {
        int corridorY = structureBounds.minY() + groundLevelDelta + 1;
        int faceX;
        int faceZ;
        if (directionToCave.getAxis() == Direction.Axis.Z) {
            faceX = (structureBounds.minX() + structureBounds.maxX()) / 2;
            faceZ = directionToCave == Direction.NORTH ? structureBounds.minZ() - 1 : structureBounds.maxZ() + 1;
        } else {
            faceZ = (structureBounds.minZ() + structureBounds.maxZ()) / 2;
            faceX = directionToCave == Direction.WEST ? structureBounds.minX() - 1 : structureBounds.maxX() + 1;
        }
        int halfWidth = tunnelHalfWidth(structureBounds, directionToCave);
        int halfHeight = tunnelHalfHeight(structureBounds);
        int corridorCenterY = corridorY - 1 + halfHeight;
        int endX = faceX + directionToCave.getStepX() * (TUNNEL_LENGTH + TUNNEL_OVERSHOOT + 1);
        int endZ = faceZ + directionToCave.getStepZ() * (TUNNEL_LENGTH + TUNNEL_OVERSHOOT + 1);
        int sidePadding = halfWidth + (int) Math.ceil(TUNNEL_WANDER + TUNNEL_BULGE) + 1;
        int verticalPadding = halfHeight + (int) Math.ceil(TUNNEL_VERTICAL_WANDER + TUNNEL_BULGE) + 1;
        int minX;
        int maxX;
        int minZ;
        int maxZ;
        if (directionToCave.getAxis() == Direction.Axis.X) {
            minX = Math.min(faceX, endX);
            maxX = Math.max(faceX, endX);
            minZ = faceZ - sidePadding;
            maxZ = faceZ + sidePadding;
        } else {
            minZ = Math.min(faceZ, endZ);
            maxZ = Math.max(faceZ, endZ);
            minX = faceX - sidePadding;
            maxX = faceX + sidePadding;
        }
        return new AABB(
                minX, corridorCenterY - verticalPadding, minZ,
                maxX + 1, corridorCenterY + verticalPadding + 1, maxZ + 1
        );
    }

    private static int tunnelHalfWidth(BoundingBox entranceBounds, Direction directionToCave) {
        int faceWidth = directionToCave.getAxis() == Direction.Axis.Z ? entranceBounds.getXSpan() : entranceBounds.getZSpan();
        return Math.clamp((faceWidth - 2) / 2, MIN_TUNNEL_HALF, MAX_TUNNEL_HALF);
    }

    private static int tunnelHalfHeight(BoundingBox entranceBounds) {
        return Math.clamp((entranceBounds.getYSpan() - 2) / 2, MIN_TUNNEL_HALF, MAX_TUNNEL_HALF);
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
        int bodyX = caveFloor.getX() + directionIntoStone.getStepX() * (TUNNEL_LENGTH + 5);
        int bodyZ = caveFloor.getZ() + directionIntoStone.getStepZ() * (TUNNEL_LENGTH + 5);
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

    private static double signedNoise(double x, double y, double z) {
        return Math.clamp(MEANDER_NOISE.getValue(x, y, z), -1.0, 1.0);
    }

    private static double noise01(double x, double y, double z) {
        return Math.clamp(WEATHER_NOISE.getValue(x, y, z) * 0.5 + 0.5, 0.0, 1.0);
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.CAVE.get();
    }
}
