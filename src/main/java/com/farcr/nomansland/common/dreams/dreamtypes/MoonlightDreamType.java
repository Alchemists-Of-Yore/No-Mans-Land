package com.farcr.nomansland.common.dreams.dreamtypes;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.dreams.MoonlightDreamRenderer;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.PlaceCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class MoonlightDreamType extends DreamType {
    public MoonlightDreamType() {
        super((player, level) -> true);

        this.setCanSprint(false)
            .setHUDHidden(false)
            .setSpawnPoint(new Vec3(0, MONOLITH_HEIGHT + 2, -20))
            .setChunkGenerator(this::moonlightChunkGenerator)
            .setRenderer(MoonlightDreamRenderer::new);
    }

    @Override public void tick() {
        super.tick();
    }

    public static final int MONOLITH_HEIGHT = 18;
    private static final int chunkSize = 16;

    @Override
    public void createStructures(
        ChunkGenerator generator,
        RegistryAccess registryAccess,
        ChunkGeneratorStructureState structureState,
        StructureManager structureManager, ChunkAccess chunk,
        StructureTemplateManager structureTemplateManager
    ) {
        ChunkPos chunkPos = chunk.getPos();
        if (new ChunkPos(new BlockPos(4, 0, 116)).equals(chunkPos)) {
            Structure structure = registryAccess.registryOrThrow(Registries.STRUCTURE).getOrThrow(
                ResourceKey.create(Registries.STRUCTURE, NoMansLand.location("dream_meeting_point"))
            );
            StructureStart structurestart = structure.generate(
                registryAccess, generator, generator.getBiomeSource(),
                structureState.randomState(), structureTemplateManager,
                structureState.getLevelSeed(), chunk.getPos(), 0, chunk, ((uh) -> true)
            );
            structureManager.setStartForStructure(SectionPos.of(new BlockPos(0, MONOLITH_HEIGHT, 0)), structure, structurestart, chunk);
        }
    }

    public void moonlightChunkGenerator(ChunkAccess chunk, StructureManager manager, WorldGenRegion level) {
        ChunkPos chunkPos = chunk.getPos();
        int startingX = chunkPos.x * chunkSize;
        int startingZ = chunkPos.z * chunkSize;

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                BlockPos blockPos = new BlockPos(startingX + x, 0, startingZ + z);
                chunk.setBlockState(blockPos.above(MONOLITH_HEIGHT + 1), Blocks.BARRIER.defaultBlockState(), false);

                int approachMax = 108;
                if (Math.abs(blockPos.getX()) <= 1 && blockPos.getZ() < approachMax) {
                    int height = (blockPos.getZ() - approachMax) + (MONOLITH_HEIGHT + 4);
                    if (height > 0) {
                        for (int i = 0; i < height; i++)
                            chunk.setBlockState(blockPos.above(i), Blocks.STONE.defaultBlockState(), false);
                        chunk.setBlockState(blockPos.above(height), Blocks.STONE_STAIRS.defaultBlockState()
                            .rotate(chunk.getLevel(), blockPos.above(height), Rotation.CLOCKWISE_180), false);
                    }
                }

                if (startingZ > 2) {
                    int checkerboardX = (Math.abs(startingX + x) + 2) / 5;
                    if (checkerboardX > 0) {
                        int checkerboardZ = (Math.abs(startingZ + z) - 3) / 5;
                        if (checkerboardX < 2 && checkerboardZ > 20 && checkerboardZ < 24)
                                continue;

                        int monolithIndex = (Math.abs(startingX + x) + 1) % 5;
                        if (monolithIndex < 3 && ((Math.abs(startingZ + z) - 3) % 5) < 1) {
                            for (int i = 1; i < MONOLITH_HEIGHT; i++)
                                chunk.setBlockState(blockPos.above(i), Blocks.STONE.defaultBlockState(), false);
                            BlockPos monolithStairPos = blockPos.above(MONOLITH_HEIGHT + 1);
                            if (monolithIndex == 1) {
                                chunk.setBlockState(blockPos.above(MONOLITH_HEIGHT), Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 10), false);
                                chunk.setBlockState(monolithStairPos, Blocks.STONE.defaultBlockState(), false);
                            } else {
                                boolean flippedStairs = monolithIndex == 0;
                                if (startingX < 0)
                                    flippedStairs = !flippedStairs;
                                chunk.setBlockState(blockPos.above(MONOLITH_HEIGHT), Blocks.STONE.defaultBlockState(), false);
                                chunk.setBlockState(monolithStairPos,
                                    Blocks.STONE_STAIRS.defaultBlockState().rotate(chunk.getLevel(), monolithStairPos,
                                        flippedStairs ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90),
                                    false
                                );
                            }
                        }
                    }
                }
            }

        }

    }
}
