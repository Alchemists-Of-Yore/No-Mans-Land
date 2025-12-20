package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CaveStructure extends Structure {
    private final Holder<StructureTemplatePool> startPool;
    private static int totalTries = 0;
    private static int successfulTries = 0;

    public static final MapCodec<CaveStructure> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool)
            ).apply(instance, CaveStructure::new)
    );

    public CaveStructure(Structure.StructureSettings settings, Holder<StructureTemplatePool> startPool) {
        super(settings);
        this.startPool = startPool;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor level = context.heightAccessor();
        RandomState randomState = context.randomState();
        RandomSource random = context.random();

        totalTries++;

        BlockPos targetPos = null;
        Direction facing = null;

        int seaLevel = generator.getSeaLevel();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();

        for (int tries = 0; tries < 32 && targetPos == null; tries++) {
            int x = minX + random.nextInt(16);
            int z = minZ + random.nextInt(16);
            NoiseColumn col = generator.getBaseColumn(x, z, level, randomState);

            int y = -30 + random.nextInt(seaLevel - 10);

            int floorY = y-1;
            while (floorY > level.getMinBuildHeight() && col.getBlock(floorY).isAir()) {
                floorY--;
            }

            if (!col.getBlock(floorY).isSolid()) continue;

            y = floorY + 1;
            if (!col.getBlock(y).isAir()) continue;
            BlockPos airPos = new BlockPos(x, y, z);

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                int dx = dir.getStepX();
                int dz = dir.getStepZ();

                NoiseColumn wallCol = generator.getBaseColumn(x - dx, z - dz, level, randomState);

                boolean solidWall = true;
                for (int dy = 0; dy < 2; dy++) {
                    if (!wallCol.getBlock(y + dy).isSolid()) {
                        solidWall = false;
                        break;
                    }
                }

                if (!solidWall) continue;

                targetPos = airPos.relative(dir.getOpposite());
                facing = dir;
                break;
            }
        }

        if (totalTries % 10 == 0) {
            double rate = (totalTries == 0) ? 0 : (100.0 * successfulTries / totalTries);
            NoMansLand.LOGGER.info(String.format(
                    "CaveStructure success rate: %.2f%% (%d / %d)", rate, successfulTries, totalTries
            ));
        }

        if (targetPos == null) return Optional.empty();
        successfulTries++;

        Rotation rotation = switch (facing) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };

        StructurePoolElement element = startPool.value().getRandomTemplate(random);
        Vec3i size = element.getSize(context.structureTemplateManager(), rotation);
        int offset = size.getX() / 2;

        BlockPos.MutableBlockPos adjustedPos = targetPos.mutable();

        switch (facing) {
            case EAST -> adjustedPos.move(0, 0, -offset);
            case WEST -> adjustedPos.move(0, 0, offset);
            case SOUTH -> adjustedPos.move(offset, 0, 0);
            default -> adjustedPos.move(-offset, 0, 0);
        }

        adjustedPos.move(facing.getOpposite(), 4+random.nextInt(5));
        NoiseColumn adjustedColumn = generator.getBaseColumn(adjustedPos.getX(), adjustedPos.getZ(), level, randomState);

        if (adjustedColumn.getBlock(adjustedPos.getY()).isAir()) return Optional.empty();

        return addCavePieces(context, this.startPool, adjustedPos, Optional.of(rotation), element);
    }

    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, PiecesContainer pieces) {
        super.afterPlace(level, structureManager, generator, random, box, chunkPos, pieces);

        if (pieces.pieces().isEmpty()) return;

        StructurePiece entrance = pieces.pieces().getFirst();
        BoundingBox pieceBox = entrance.getBoundingBox();
        BlockPos centre = pieceBox.getCenter();

        Direction facing = switch (entrance.getRotation()) {
            case CLOCKWISE_90 -> Direction.EAST;
            case CLOCKWISE_180 -> Direction.SOUTH;
            case COUNTERCLOCKWISE_90 -> Direction.WEST;
            default -> Direction.NORTH;
        };

        int length = switch (facing) {
            case EAST, WEST -> pieceBox.getXSpan();
            default -> pieceBox.getZSpan();
        };

        BlockPos.MutableBlockPos cursor = centre.mutable().move(facing, length / 2);
        List<BlockPos> carved = new ArrayList<>();

        int maxLength = 300;
        int steps = 0;
        int verticalOffset = 0;

        int halfWidth = 3 + random.nextInt(3);
        int halfHeight = 2 + random.nextInt(3);
        int widthHold = 0;
        int heightHold = 0;

        int horizontalDrift = 0;
        int driftTimer = 0;

        while (steps < maxLength) {
            if (!level.getBlockState(cursor).isSolid()) break;

            if (random.nextFloat() < 0.1f) {
                verticalOffset += random.nextBoolean() ? 1 : -1;
                verticalOffset = Mth.clamp(verticalOffset, -2, 2);
            }

            if (driftTimer <= 0 && random.nextFloat() < 0.15f) {
                horizontalDrift = random.nextBoolean() ? 1 : -1;
                driftTimer = 5 + random.nextInt(10);
            } else if (driftTimer > 0) {
                driftTimer--;
            } else {
                horizontalDrift = 0;
            }

            BlockPos.MutableBlockPos tunnelCenter = cursor.mutable()
                    .move(Direction.UP, verticalOffset);

            if (horizontalDrift != 0) {
                if (facing.getAxis() == Direction.Axis.X)
                    tunnelCenter.move(Direction.NORTH, horizontalDrift);
                else
                    tunnelCenter.move(Direction.EAST, horizontalDrift);
            }

            if (widthHold <= 0) {
                if (random.nextFloat() < 0.1f) {
                    halfWidth = 3 + random.nextInt(2);
                }
                widthHold = 3 + random.nextInt(4);
            } else {
                widthHold--;
            }

            if (heightHold <= 0) {
                if (random.nextFloat() < 0.1f) {
                    halfHeight = 3 + random.nextInt(2);
                }
                heightHold = 3 + random.nextInt(4);
            } else {
                heightHold--;
            }

            for (int x = -halfWidth; x <= halfWidth; x++) {
                for (int y = -1; y <= halfHeight; y++) {
                    BlockPos.MutableBlockPos carvePos = tunnelCenter.mutable();
                    if (facing.getAxis() == Direction.Axis.X)
                        carvePos.move(Direction.NORTH, x);
                    else
                        carvePos.move(Direction.EAST, x);
                    carvePos.move(Direction.UP, y);

                    BlockState state = level.getBlockState(carvePos);
                    if (state.isSolid()) {
                        level.setBlock(carvePos, Blocks.AIR.defaultBlockState(), 2);
                        carved.add(carvePos.immutable());
                    }

                    if (y == 0 && random.nextInt(6) == 0 && level.getBlockState(carvePos.below()).isSolid()) {
                        level.setBlock(carvePos, NMLBlocks.CAVE_WEEDS.get().defaultBlockState(), 2);
                    }
                }
            }

            cursor.move(facing);
            steps++;
        }
    }

    public static Optional<Structure.GenerationStub> addCavePieces(Structure.GenerationContext context, Holder<StructureTemplatePool> startPool, BlockPos pos, Optional<Rotation> rotationOpt, StructurePoolElement startingElement) {
        RegistryAccess registryAccess = context.registryAccess();
        ChunkGenerator generator = context.chunkGenerator();
        StructureTemplateManager templates = context.structureTemplateManager();
        LevelHeightAccessor heightAccessor = context.heightAccessor();
        RandomSource random = context.random();
        Registry<StructureTemplatePool> poolRegistry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL);

        Rotation rotation = rotationOpt.orElse(Rotation.getRandom(random));

        if (startingElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }

        PoolElementStructurePiece rootPiece = new PoolElementStructurePiece(
                templates,
                startingElement,
                pos,
                startingElement.getGroundLevelDelta(),
                rotation,
                startingElement.getBoundingBox(templates, pos, rotation),
                LiquidSettings.IGNORE_WATERLOGGING
        );

        BoundingBox box = rootPiece.getBoundingBox();
        int centerX = (box.minX() + box.maxX()) / 2;
        int centerZ = (box.minZ() + box.maxZ()) / 2;

        int k = pos.getY();
        int groundY = box.minY() + rootPiece.getGroundLevelDelta();
        rootPiece.move(0, k - groundY, 0);

        return Optional.of(new Structure.GenerationStub(new BlockPos(centerX, k, centerZ), pieces -> {
            List<PoolElementStructurePiece> list = new ArrayList<>();
            list.add(rootPiece);

            int maxDistance = 116;

            AABB region = new AABB(
                    centerX - maxDistance, Math.max(k - maxDistance, heightAccessor.getMinBuildHeight()),
                    centerZ - maxDistance,
                    centerX + maxDistance + 1, Math.min(k + maxDistance + 1, heightAccessor.getMaxBuildHeight()),
                    centerZ + maxDistance + 1
            );

            VoxelShape shape = Shapes.join(
                    Shapes.create(region),
                    Shapes.create(AABB.of(box)),
                    BooleanOp.ONLY_FIRST
            );

            try {
                addPieces(context.randomState(), generator, templates, heightAccessor, random, poolRegistry, rootPiece, list, shape);
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException ignored) {
                System.out.println("oopsie error");
            }

            list.forEach(pieces::addPiece);
        }));
    }

    public static void addPieces(RandomState randomState, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, LevelHeightAccessor level, RandomSource random, Registry<StructureTemplatePool> pools, PoolElementStructurePiece startPiece, List<PoolElementStructurePiece> pieces, VoxelShape free) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        JigsawPlacement.Placer jigsawplacement$placer = new JigsawPlacement.Placer(pools, 20, chunkGenerator, structureTemplateManager, pieces, random);
        var placer = new JigsawPlacement.Placer(pools, 20, chunkGenerator, structureTemplateManager, pieces, random);
//        Method m = JigsawPlacement.Placer.class.getDeclaredMethod(
//                "tryPlacingChildren",
//                PoolElementStructurePiece.class,
//                MutableObject.class,
//                int.class,
//                boolean.class,
//                LevelHeightAccessor.class,
//                RandomState.class,
//                PoolAliasLookup.class,
//                LiquidSettings.class
//        );
//        m.setAccessible(true);
//        m.invoke(placer, startPiece, new MutableObject<>(free), 0, false, level, randomState, PoolAliasLookup.EMPTY, LiquidSettings.IGNORE_WATERLOGGING);

//        jigsawplacement$placer.tryPlacingChildren(startPiece, new MutableObject<>(free), 0, false, level, randomState, PoolAliasLookup.EMPTY, LiquidSettings.IGNORE_WATERLOGGING);

        while(jigsawplacement$placer.placing.hasNext()) {
            JigsawPlacement.PieceState jigsawplacement$piecestate = jigsawplacement$placer.placing.next();
//            m.invoke(placer, jigsawplacement$piecestate.piece(), jigsawplacement$piecestate.free(), jigsawplacement$piecestate.depth(), false, level, randomState, PoolAliasLookup.EMPTY, LiquidSettings.IGNORE_WATERLOGGING);

//            jigsawplacement$placer.tryPlacingChildren(jigsawplacement$piecestate.piece(), jigsawplacement$piecestate.free(), jigsawplacement$piecestate.depth(), false, level, randomState, PoolAliasLookup.EMPTY, LiquidSettings.IGNORE_WATERLOGGING);
        }
    }


    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.CAVE.get();
    }
}
