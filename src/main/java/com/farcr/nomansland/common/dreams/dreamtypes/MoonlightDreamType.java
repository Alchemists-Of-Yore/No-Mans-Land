package com.farcr.nomansland.common.dreams.dreamtypes;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.dreams.MoonlightDreamRenderer;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class MoonlightDreamType extends DreamType {
    public MoonlightDreamType() {
        super((player, level) -> true);

        this.setCanSprint(false)
            .setHUDHidden(false)
            .setSpawnPoint(new Vec3(0, 2, -20))
            .setChunkGenerator(this::moonlightChunkGenerator)
            .setRenderer(MoonlightDreamRenderer::new);
    }

    @Override public void tick() {
        super.tick();
    }

    private static int MONOLITH_HEIGHT = 18;

    private void generateStructure(ChunkAccess chunkAccess, BlockPos pos, StructureManager manager, WorldGenRegion level) {
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getOrThrow(
            ResourceKey.create(Registries.STRUCTURE, NoMansLand.location("meeting_point"))
        );
        ServerLevel serverLevel = level.getLevel();
        ChunkGenerator generator = level.getLevel().getChunkSource().getGenerator();
        ChunkPos chunkPos = new ChunkPos(pos);
        StructureStart structurestart = structure.generate(
            level.registryAccess(), generator, generator.getBiomeSource(),
            serverLevel.getChunkSource().randomState(), level.getServer().getStructureManager(),
            level.getSeed(), chunkPos, 0, chunkAccess, (uh) -> true
        );
        manager.setStartForStructure(SectionPos.of(pos), structure, structurestart, chunkAccess);
    }

    public void moonlightChunkGenerator(ChunkAccess chunk, StructureManager manager, WorldGenRegion level) {
        int chunkSize = 16;
        ChunkPos chunkPos = chunk.getPos();
        int startingX = chunkPos.x * chunkSize;
        int startingZ = chunkPos.z * chunkSize;

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                BlockPos blockPos = new BlockPos(startingX + x, 0, startingZ + z);
                chunk.setBlockState(blockPos, Blocks.ORANGE_CONCRETE.defaultBlockState(), false);

                if (startingZ > 2) {
                    int checkerboardX = (Math.abs(startingX + x) + 2) / 5;
                    if (checkerboardX > 0) {
                        int checkerboardZ = (Math.abs(startingZ + z) - 3) / 5;
                        if (checkerboardX < 2 && checkerboardZ > 20 && checkerboardZ < 24) {
                            if (x == 5 && z == 0) generateStructure(chunk, blockPos, manager, level);
                            continue;
                        }

                        int monolithIndex = (Math.abs(startingX + x) + 1) % 5;
                        if (monolithIndex < 3 && ((Math.abs(startingZ + z) - 3) % 5) < 1) {
                            for (int i = 1; i < MONOLITH_HEIGHT; i++)
                                chunk.setBlockState(blockPos.above(i), Blocks.STONE.defaultBlockState(), false);
                            BlockPos monolithStairPos = blockPos.above(MONOLITH_HEIGHT + 1);
                            if (monolithIndex == 1) {
                                chunk.setBlockState(blockPos.above(MONOLITH_HEIGHT), Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), false);
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
