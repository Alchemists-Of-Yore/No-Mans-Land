package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.world.structure.cave.CaveContext;
import com.farcr.nomansland.common.world.structure.cave.CaveEncasing;
import com.farcr.nomansland.common.world.structure.cave.CaveErosion;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;

public abstract class EmbeddedStructure extends PreservableStructure {
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

    protected static final HeightProvider DEFAULT_START_HEIGHT = UniformHeight.of(VerticalAnchor.absolute(-48), VerticalAnchor.absolute(-8));
    protected static final int MIN_DEPTH_BELOW_SURFACE = 5;
    protected static final int MIN_HEIGHT_ABOVE_WORLD_BOTTOM = 10;
    private static final int BOUNDING_BOX_INFLATION = 20;

    protected final Holder<StructureTemplatePool> startPool;
    protected final int maxDepth;
    protected final int maxDistanceFromCenter;
    protected final HeightProvider startHeight;
    protected final Optional<Heightmap.Types> projectStartToHeightmap;
    protected final ExposureTreatment exposure;
    private final boolean preserve;

    protected EmbeddedStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxDepth, int maxDistanceFromCenter, HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap, ExposureTreatment exposure, boolean preserve) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.exposure = exposure;
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
    protected void afterPlaceStructure(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBounds, ChunkPos chunkPos, PiecesContainer pieces) {
        if (pieces.pieces().isEmpty()) return;
        if (!(pieces.pieces().get(0) instanceof PoolElementStructurePiece startPiece)) return;
        if (!chunkBounds.intersects(affectedRegion(pieces, startPiece))) return;

        CaveContext context = new CaveContext(level, chunkBounds, pieces);
        if (this.exposure == ExposureTreatment.ENCASE) CaveEncasing.apply(context);
        carveTunnel(context, startPiece);
        if (this.exposure == ExposureTreatment.ERODE) CaveErosion.apply(context);
    }

    protected BoundingBox affectedRegion(PiecesContainer pieces, PoolElementStructurePiece startPiece) {
        return pieces.calculateBoundingBox().inflatedBy(CaveEncasing.affectedReach());
    }

    protected void carveTunnel(CaveContext context, PoolElementStructurePiece startPiece) {
    }
}
