package com.farcr.nomansland.common.handler.sanctuary_grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;

/**
 * A cell containing a pair of Bell Sanctuary {@link ChunkPos section positions}.
 */
public class BellSanctuaryCell implements Iterable<ChunkPos> {

    public static Codec<BellSanctuaryCell> CODEC = RecordCodecBuilder.create(i ->
            i.group(Codec.INT.fieldOf("x").forGetter(c -> c.x),
                    Codec.INT.fieldOf("z").forGetter(c -> c.z),
                    Codec.BOOL.fieldOf("valid").forGetter(c -> c.valid),
                    Codec.BOOL.fieldOf("generated").forGetter(c -> c.attemptedToGenerate),
                    Codec.LONG.optionalFieldOf("firstBellSanctuaryPos").forGetter(c ->
                            c.firstBellSanctuaryPos == null ? Optional.empty() : Optional.of(c.firstBellSanctuaryPos.toLong())),
                    Codec.LONG.optionalFieldOf("secondBellSanctuaryPos").forGetter(c ->
                            c.secondBellSanctuaryPos == null ? Optional.empty() : Optional.of(c.secondBellSanctuaryPos.toLong()))
            ).apply(i, (x, z, valid, generated, first, second) -> {
                BellSanctuaryCell cell = new BellSanctuaryCell(x, z);

                cell.valid = valid;
                cell.attemptedToGenerate = generated;
                if (valid) {
                    cell.firstBellSanctuaryPos = new ChunkPos(first.get());
                    cell.secondBellSanctuaryPos = new ChunkPos(second.get());
                }

                return cell;
            })
    );

    /**
     * The minimum distance allowed between <i>any<i/> two Bell Sanctuary position.
     */
    public static final int MIN_CHUNK_DISTANCE = 60;

    /**
     * The maximum distance allowed between <i>any<i/> two Bell Sanctuary positions.
     */
    public static final int MAX_CHUNK_DISTANCE = 350;

    /**
     * This cell's X position
     */
    public final int x;

    /**
     * This cell's Z position
     */
    public final int z;

    /**
     * Whether This cell was able to generate A valid pairing of Bell Sanctuary positions.
     */
    private boolean valid = false;

    /**
     * Whether this cell has attempted to generate or not.
     */
    private boolean attemptedToGenerate = false;

    /**
     * The first valid Bell Sanctuary position.
     */
    @Nullable
    private ChunkPos firstBellSanctuaryPos;

    /**
     * The second valid Bell Sanctuary position.
     */
    @Nullable
    private ChunkPos secondBellSanctuaryPos;

    public BellSanctuaryCell(final int x, final int z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Attempts to generate a valid pairing of Bell Sanctuary positions.
     *
     * @param levelSeed The seed to base generation off of.
     */
    @Contract(mutates = "this")
    public void generatePositionsNoBiome(final long levelSeed) {
        final long newSeed = (long) this.x * 341873128712L + (long) this.z * 132897987541L + levelSeed;
        final RandomSource random = RandomSource.create(newSeed);

        for (int i = 0; i < 10000; i++) {
            final ChunkPos firstChunkPos = this.generateRandomChunkPos(random);
            final ChunkPos secondChunkPos = this.generateRandomChunkPos(random);

            final int dist = firstChunkPos.distanceSquared(secondChunkPos);
            if (dist < MIN_CHUNK_DISTANCE * MIN_CHUNK_DISTANCE || dist > MAX_CHUNK_DISTANCE * MAX_CHUNK_DISTANCE) {
                continue;
            }

            this.firstBellSanctuaryPos = firstChunkPos;
            this.secondBellSanctuaryPos = secondChunkPos;
            break;
        }

        if (this.firstBellSanctuaryPos != null && this.secondBellSanctuaryPos != null) {
            this.valid = true;
        }

        this.attemptedToGenerate = true;
    }

    private @NotNull ChunkPos generateRandomChunkPos(final RandomSource random) {
        return new ChunkPos(
                (int) ((this.x * BellSanctuaryGrid.CELL_SIDE_CHUNK_LENGTH) + (BellSanctuaryGrid.CELL_SIDE_CHUNK_LENGTH - MIN_CHUNK_DISTANCE) * random.nextDouble()),
                (int) ((this.z * BellSanctuaryGrid.CELL_SIDE_CHUNK_LENGTH) + (BellSanctuaryGrid.CELL_SIDE_CHUNK_LENGTH - MIN_CHUNK_DISTANCE) * random.nextDouble())
        );
    }

    public boolean hasAttemptedToGenerate() {
        return this.attemptedToGenerate;
    }

    public boolean isValid() {
        return this.valid;
    }

    //TODO:replace when BellSanctuary structure is created
    public boolean validGenChunk(final int checkChunkX, final int checkChunkZ) {
        final ChunkPos checkPos = new ChunkPos(checkChunkX, checkChunkZ);
        return checkPos.equals(this.firstBellSanctuaryPos) || checkPos.equals(this.secondBellSanctuaryPos);
    }

    public @Nullable ChunkPos getFirstBellSanctuaryPos() {
        if (this.isValid()) {
            assert this.firstBellSanctuaryPos != null : "Cell is valid, and yet the first sanctuary position is null. How??";
        }
        
        return this.firstBellSanctuaryPos;
    }

    public @Nullable ChunkPos getSecondBellSanctuaryPos() {
        if (this.isValid()) {
            assert this.secondBellSanctuaryPos != null : "Cell is valid, and yet the second sanctuary position is null. How??";
        }

        return this.secondBellSanctuaryPos;
    }

    @Override
    public @NotNull Iterator<ChunkPos> iterator() {
        return new BellSanctuaryIterator();
    }

    @ApiStatus.Internal
    public static BellSanctuaryCell deserialize(final CompoundTag data) {
        return CODEC.decode(NbtOps.INSTANCE, data).getOrThrow().getFirst();
    }

    @ApiStatus.Internal
    public Tag serialize() {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }

    @ApiStatus.Internal
    private class BellSanctuaryIterator implements Iterator<ChunkPos> {
        private byte i = 0;

        @Override
        public boolean hasNext() {
            return BellSanctuaryCell.this.isValid() && this.i < 2;
        }

        @Override
        public ChunkPos next() {
            if (!BellSanctuaryCell.this.isValid()) {
                return null;
            }

            if (this.i == 0) {
                this.i++;
                return BellSanctuaryCell.this.firstBellSanctuaryPos;
            } else {
                this.i++;
                return BellSanctuaryCell.this.secondBellSanctuaryPos;
            }
        }
    }
}
