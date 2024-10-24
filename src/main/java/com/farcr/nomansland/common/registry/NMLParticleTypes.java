package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NMLParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, NoMansLand.MODID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PALE_CHERRY_LEAVES = PARTICLE_TYPES.register("pale_cherry_leaves",
            () -> new SimpleParticleType(false) {
            });
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CAVE_DUST = PARTICLE_TYPES.register("cave_dust",
            () -> new SimpleParticleType(false) {
            });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RESIN_DROPLET = PARTICLE_TYPES.register("resin_droplet",
            () -> new SimpleParticleType(false) {
            });
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RESIN_DROPLET_FLAT = PARTICLE_TYPES.register("resin_droplet_flat",
            () -> new SimpleParticleType(false) {
            });
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MAPLE_SYRUP_DROPLET = PARTICLE_TYPES.register("maple_syrup_droplet",
            () -> new SimpleParticleType(false) {
            });
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MAPLE_SYRUP_DROPLET_FLAT = PARTICLE_TYPES.register("maple_syrup_droplet_flat",
            () -> new SimpleParticleType(false) {
            });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> OIL = PARTICLE_TYPES.register("oil",
            () -> new SimpleParticleType(false) {
            });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> OIL_FLAT = PARTICLE_TYPES.register("oil_flat",
            () -> new SimpleParticleType(false) {
            });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RESIN_OIL_BUBBLE = PARTICLE_TYPES.register("resin_oil_bubble",
            () -> new SimpleParticleType(false) {
            });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RESIN_OIL_BUBBLE_POP = PARTICLE_TYPES.register("resin_oil_bubble_pop",
            () -> new SimpleParticleType(false) {
            });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MALEVOLENT_FLAME = PARTICLE_TYPES.register("malevolent_flame",
            () -> new SimpleParticleType(false) {
            });
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MALEVOLENT_EMBERS = PARTICLE_TYPES.register("malevolent_embers",
            () -> new SimpleParticleType(false) {
            });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SCULK_AMBIENCE = PARTICLE_TYPES.register("sculk_ambience",
            () -> new SimpleParticleType(false) {
            });
}
