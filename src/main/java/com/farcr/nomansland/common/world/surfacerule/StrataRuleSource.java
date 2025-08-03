package com.farcr.nomansland.common.world.surfacerule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record StrataRuleSource(List<BlockState> strataEntries, boolean jumbleEntries) implements SurfaceRules.RuleSource {
    public static final KeyDispatchDataCodec<StrataRuleSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                    record -> record.group(
                            BlockState.CODEC.listOf().fieldOf("strata_entries").forGetter(StrataRuleSource::strataEntries),
                            Codec.BOOL.fieldOf("jumble_entries").forGetter(StrataRuleSource::jumbleEntries)
                    ).apply(record, StrataRuleSource::new)
            )
    );

    public StrataRuleSource(boolean jumbleEntries, BlockState... strataEntries) {
        this(List.of(strataEntries), jumbleEntries);
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        return jumbleEntries ? new JumbledStrataRule(context, strataEntries) : new StrataRule(context, strataEntries);
    }

    private record StrataRule(SurfaceRules.Context context, List<BlockState> strataEntries) implements SurfaceRules.SurfaceRule {
        @Override
        public @Nullable BlockState tryApply(int x, int y, int z) {
            int offset = (int) Math.round(context.system.clayBandsOffsetNoise.getValue(x, 0, z) * 4.0);
            int index = Math.floorMod(y + offset, strataEntries.size());
            return strataEntries.get(index);
        }
    }

    private record JumbledStrataRule(SurfaceRules.Context context, List<BlockState> strataEntries) implements SurfaceRules.SurfaceRule {
        @Override
        public @Nullable BlockState tryApply(int x, int y, int z) {
            int offset = (int) Math.round(context.system.clayBandsOffsetNoise.getValue(x, 0, z) * 4.0);
            int index = Math.floorMod(lowBiasHash(y + offset), strataEntries.size());
            return strataEntries.get(index);
        }
    }

    private static int lowBiasHash(int num) {
        num ^= num >> 16;
        num *= 0x7feb352d;
        num ^= num >> 15;
        num *= 0x846ca68b;
        num ^= num >> 16;
        return num;
    }
}
