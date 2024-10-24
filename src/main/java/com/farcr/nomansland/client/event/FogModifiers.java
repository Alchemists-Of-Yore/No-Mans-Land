package com.farcr.nomansland.client.event;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = NoMansLand.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class FogModifiers {
    //Made with infinite help from big man Cappin
    //This class definitely isn't the cleanest code or uses the best practices but its effects are too good to give up on.

    private static final float WeatherTransition = 0;
    public static Holder<Biome> biome;
    //TODO: add config
    private static float deepDarkTransition = 0;
    private static float FoggyBiomeTransition = 0;
    private static int timer = 0;

    @SubscribeEvent
    public static void getBiome(ClientTickEvent.Pre event) {
        timer++;
        if (timer >= 60) {
            timer = 0;
            Entity player = Minecraft.getInstance().gameRenderer.getMainCamera().getEntity();
            if (player == null) return;
            Level world = player.level();
            BiomeManager biomemanager = world.getBiomeManager();
            Vec3 cameraInBiomeSpace = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
            biome = biomemanager.getNoiseBiomeAtQuart((int) cameraInBiomeSpace.x, (int) cameraInBiomeSpace.y, (int) cameraInBiomeSpace.z);
        }
        //If this applies every tick your fps get fucked so this only happens once every 3 seconds
    }

    @SubscribeEvent
    public static void everywhereFog(ViewportEvent.RenderFog event) {
        event.setCanceled(true);
        event.scaleNearPlaneDistance(0.15f);
        if (biome == null || !NMLConfig.FOG_MODIFIERS.get()) return;
        //Deep Dark
        if (biome.is(Biomes.DEEP_DARK)) {
            deepDarkTransition = Mth.lerp(0.005f, deepDarkTransition, 1.0f);
        } else {
            deepDarkTransition = Mth.lerp(0.005f, deepDarkTransition, 0.0f);
        }
        if (deepDarkTransition > 0.01) {
            event.scaleNearPlaneDistance(Mth.lerp(deepDarkTransition, 1.0f, -2f));
        }
        //Dense Fog Biomes
        if (biome.is(NMLTags.HAS_DENSE_FOG)) {
            FoggyBiomeTransition = Mth.lerp(0.005f, FoggyBiomeTransition, 1.0f);
        } else {
            FoggyBiomeTransition = Mth.lerp(0.005f, FoggyBiomeTransition, 0.0f);
        }
        if (FoggyBiomeTransition > 0.01) {
            event.scaleNearPlaneDistance(Mth.lerp(FoggyBiomeTransition, 1.0f, -0.25f));
        }

    }
}
