package com.farcr.nomansland.common.world.surfacerule;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class NMLSteepMaterialCondition {
    public static boolean evaluate(SurfaceRules.Context context) {
        ChunkAccess chunkaccess = context.chunk;
        int chunkX = context.blockX & 15,
            chunkZ = context.blockZ & 15;
        int lastZ = Math.max(chunkZ - 1, 0),
            nextZ = Math.min(chunkZ + 1, 15);
        int lastZHeight = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, chunkX, lastZ),
            nextZHeight = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, chunkX, nextZ);
        int zHeightDiff = Math.abs(lastZHeight - nextZHeight);
        if (zHeightDiff >= 4 /*&& Math.abs(height - lastZHeight) != 0 && Math.abs(height - nextZHeight) != 0*/) return true;

        int lastX = Math.max(chunkX - 1, 0),
            nextX = Math.min(chunkX + 1, 15);
        int lastXHeight = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, lastX, chunkZ),
            nextXHeight = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, nextX, chunkZ);
        int xHeightDiff = Math.abs(lastXHeight - nextXHeight);
        if (xHeightDiff >= 4 /*&& Math.abs(height - lastXHeight) != 0 && Math.abs(height - nextXHeight) != 0*/) return true;

        return false;
    }
}
