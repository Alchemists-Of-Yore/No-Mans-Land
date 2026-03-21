package com.farcr.nomansland.common.dreams.dreamtypes;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.DreamType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

public class MoonlightDreamType extends DreamType {
    public MoonlightDreamType() {
        super((player, level) -> false);

        this.setCanSprint(false);
        this.setHUDHidden(false);
        this.setSpawnPoint(new Vec3(0, 2, -20));
        this.setChunkGenerator(this::moonlightChunkGenerator);
    }

    public void moonlightChunkGenerator(ChunkAccess chunk) {
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
                            continue;
                        }

                        int monolithIndex = (Math.abs(startingX + x) + 1) % 5;
                        if (monolithIndex < 3 && ((Math.abs(startingZ + z) - 3) % 5) < 1) {
                            int monolithHeight = 18;
                            for (int i = 1; i < monolithHeight; i++)
                                chunk.setBlockState(blockPos.above(i), Blocks.STONE.defaultBlockState(), false);
                            BlockPos monolithStairPos = blockPos.above(monolithHeight + 1);
                            if (monolithIndex == 1) {
                                chunk.setBlockState(blockPos.above(monolithHeight), Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), false);
                                chunk.setBlockState(monolithStairPos, Blocks.STONE.defaultBlockState(), false);
                            } else {
                                boolean flippedStairs = monolithIndex == 0;
                                if (startingX < 0)
                                    flippedStairs = !flippedStairs;
                                chunk.setBlockState(blockPos.above(monolithHeight), Blocks.STONE.defaultBlockState(), false);
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
