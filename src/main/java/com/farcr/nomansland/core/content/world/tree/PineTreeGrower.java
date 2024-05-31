//package com.farcr.nomansland.core.content.world.tree;
//
//import com.farcr.nomansland.core.NoMansLand;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.level.block.grower.AbstractTreeGrower;
//import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
//import org.jetbrains.annotations.Nullable;
//
//public class PineTreeGrower extends AbstractTreeGrower {
//    public static final ResourceKey<ConfiguredFeature<?, ?>> PINE = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(NoMansLand.MODID, "pine"));
//
//    @Nullable
//    @Override
//    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom, boolean pHasFlowers) {
//        return PINE;
//    }
//}
