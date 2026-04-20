package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.common.block.InvertedBellBlock;
import com.farcr.nomansland.common.handler.InvertedBellServerHandler;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryCell;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGrid;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGridHandler;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class InvertedBellControllerBlockEntity extends BlockEntity {
    public static final TicketType<ChunkPos> BELL_TICKET = TicketType.create("nml:inverted_bell", Comparator.comparingLong(ChunkPos::toLong), 300);

    public FAIL_TYPE failureType = FAIL_TYPE.NO_FAIL;

    public static final int COOLDOWN = 100;

    public PositionState state = PositionState.DONT_SEARCH;

    private @Nullable ChunkPos targetArea;
    // slowly escalates chunk ticket level to spread generation out over time
    private int escalationTimer = 10;
    private int escalationValue = 0;

    public @Nullable BlockPos targetBell;
    public @Nullable Direction targetDir;

    public int ringCooldown = 0;

    private boolean beingDestroyed = false;

    public InvertedBellControllerBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(NMLBlockEntities.INVERTED_BELL.get(), pos, blockState);
    }

    public void destroyBell() {
        // no cascading block updates
        if (this.beingDestroyed) {
            return;
        }
        this.beingDestroyed = true;

        final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = -1; y < 2; y++) {
                    if (this.level.getBlockState(mutPos.setWithOffset(this.getBlockPos(), x, y, z)).is(NMLBlocks.INVERTED_BELL.block())) {
                        this.level.destroyBlock(mutPos, false);
                    }
                }
            }
        }
    }

    public void ring(final int direction) {
        if (this.getLevel() instanceof final ServerLevel serverLevel) {
            serverLevel.playSound(null, this.getBlockPos(), NMLSounds.INVERTED_BELL_RING.get(), SoundSource.BLOCKS, 0.8f, 1.0f);
            if (this.state == PositionState.BLOCK_POS && this.targetBell != null && serverLevel.getWorldBorder().isWithinBounds(this.targetBell)) {
                InvertedBellServerHandler.get(serverLevel).beginTeleport(serverLevel,
                        this.getBlockPos(), this.getBlockState().getValue(InvertedBellBlock.HORIZONTAL_FACING),
                        this.targetBell, this.targetDir
                );
            }
            this.ringCooldown = COOLDOWN;
        } else {
            InvertedBellClientHandler.instance.onHit(direction);
        }
    }

    public void link(final InvertedBellControllerBlockEntity other) {
        this.targetBell = other.getBlockPos();
        this.targetDir = other.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        this.state = PositionState.BLOCK_POS;

        other.targetBell = this.getBlockPos();
        other.targetDir = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        other.state = PositionState.BLOCK_POS;
    }

    @Override
    public boolean triggerEvent(final int id, final int type) {
        if (id == 1) {
            this.ring(type - 1);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    public static void tick(final Level level, final BlockPos pos, final BlockState state, final InvertedBellControllerBlockEntity ibbe) {
        if (ibbe.ringCooldown > 0) {
            ibbe.ringCooldown--;
        }

        if (level.isClientSide) {
            RandomSource random = level.getRandom();
            double x = pos.getX() + random.nextDouble() * 1.375 - 0.1875;
            double y = pos.getY();// + random.nextDouble() * 1.6875 - 0.875;
            double z = pos.getZ() + random.nextDouble() * 1.375 - 0.1875;
            level.addParticle(NMLParticleTypes.ENTROPY_DUST.get(), x, y, z, 0, 0, 0);
        }

        if (level instanceof final ServerLevel serverLevel) {
            switch (ibbe.state) {
                case CHUNK -> handleAwaitingTheSearch(ibbe, pos, serverLevel);

                case UNASSIGNED -> {
                    final BellSanctuaryGrid grid = BellSanctuaryGridHandler.getGrid(serverLevel.getSeed());
                    ibbe.targetArea = getLikelyOtherSanctuary(grid, pos);
                    if (ibbe.targetArea != null) {
                        ibbe.state = PositionState.CHUNK;
                    } else {
                        ibbe.failureType = FAIL_TYPE.NO_PAIRING;
                        ibbe.state = PositionState.DONT_SEARCH;
                    }

                    if (ibbe.failureType != FAIL_TYPE.NO_FAIL) {
                        NoMansLand.LOGGER.error("{} : {}", ibbe.failureType, ibbe.failureType.failMessage);
                    }
                }
            }
        }
    }

    @Nullable
    private static ChunkPos getLikelyOtherSanctuary(final BellSanctuaryGrid grid, final BlockPos pos) {
        // scan the neighbours for pair queries, as the bell can land next to the structure's placement chunk rather than in it
        final ChunkPos beChunk = new ChunkPos(pos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                final ChunkPos candidate = new ChunkPos(beChunk.x + dx, beChunk.z + dz);
                final BellSanctuaryCell cell = grid.getCell(candidate.getMinBlockX(), candidate.getMinBlockZ());
                if (cell == null) continue;
                final BellSanctuaryCell.SanctuaryPair pair = cell.getPair(candidate);
                if (pair != null) {
                    return pair.getOther(candidate);
                }
            }
        }
        return null;
    }

    private static void handleAwaitingTheSearch(final InvertedBellControllerBlockEntity ibbe, final BlockPos pos, final ServerLevel serverLevel) {
        if (ibbe.escalationValue < ChunkPyramid.GENERATION_PYRAMID.steps().size()) {
            ibbe.escalationTimer++;
            if (ibbe.escalationTimer > 10) {
                serverLevel.getChunkSource().addRegionTicket(BELL_TICKET, ibbe.targetArea, 0, ibbe.targetArea);
                ibbe.escalationTimer = 0;
                ibbe.escalationValue++;
            }
        } else {
            final InvertedBellControllerBlockEntity otherIbbe = handleTheSearch(serverLevel, ibbe.targetArea);
            if (otherIbbe != null) {
                ibbe.link(otherIbbe);
            } else {
                ibbe.failureType = FAIL_TYPE.NO_BLOCK_ENTITY_POS;
                NoMansLand.LOGGER.error(ibbe.failureType);
                NoMansLand.LOGGER.error("Inverted Bell at {} || {} failed to find paired bell block position around chunk {} || {}", pos, new ChunkPos(pos), ibbe.targetArea.getBlockAt(8, 0, 8), ibbe.targetArea);
                ibbe.state = PositionState.DONT_SEARCH;
            }
        }
    }

    private static @Nullable InvertedBellControllerBlockEntity handleTheSearch(final ServerLevel level, final ChunkPos target) {
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                final LevelChunk chunk = level.getChunk(target.x + x, target.z + z);
                for (final BlockPos bePos : chunk.getBlockEntitiesPos()) {
                    if (chunk.getBlockEntity(bePos) instanceof final InvertedBellControllerBlockEntity ibbe) {
                        return ibbe;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("State", this.state.ordinal());

        switch (this.state) {
            case CHUNK -> {
                tag.putInt("chunkX", this.targetArea.x);
                tag.putInt("chunkZ", this.targetArea.z);
            }
            case BLOCK_POS -> {
                tag.putInt("targetX", this.targetBell.getX());
                tag.putInt("targetY", this.targetBell.getY());
                tag.putInt("targetZ", this.targetBell.getZ());
                tag.putInt("targetOrientation", this.targetDir.get2DDataValue());
            }
        }
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        final int state = tag.getInt("State");
        if (state >= 0 && state < PositionState.values().length) {
            this.state = PositionState.values()[state];
        }
        switch (this.state) {
            case CHUNK -> {
                this.targetArea = new ChunkPos(
                        tag.getInt("chunkX"),
                        tag.getInt("chunkZ")
                );
            }
            case BLOCK_POS -> {
                this.targetBell = new BlockPos(
                        tag.getInt("targetX"),
                        tag.getInt("targetY"),
                        tag.getInt("targetZ")
                );
                this.targetDir = Direction.from2DDataValue(tag.getInt("targetOrientation"));
            }
        }
    }

    public enum PositionState {
        /**
         * Default state when placed by a player, or fallback state if the linking process fails
         */
        DONT_SEARCH,
        /**
         * State when just placed by world gen. References its {@link BellSanctuaryCell} to get an approximate target position and switches to {@link PositionState#CHUNK}
         */
        UNASSIGNED,
        /**
         * State when slowly generating chunks at its target area. Once complete, searches for another block entity within those chunks and switches to {@link PositionState#BLOCK_POS}
         */
        CHUNK,
        /**
         * State when a link has been established. The bell can be interacted with and used to teleport
         */
        BLOCK_POS
    }

    public enum FAIL_TYPE {
        NO_BLOCK_ENTITY_POS("Unable to find block entity associated with the cell."), NO_CELL("The requested cell does not exist"), NO_PAIRING("The requested pairing does not exist"),  NO_FAIL("No failure");

        public final String failMessage;

        FAIL_TYPE(final String s) {
            this.failMessage = s;
        }
    }
}
