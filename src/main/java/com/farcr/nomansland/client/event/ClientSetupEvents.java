package com.farcr.nomansland.client.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLArmorModels;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.ambience.AmbienceHandler;
import com.farcr.nomansland.client.extensions.NMLClientExtensions;
import com.farcr.nomansland.client.music.ContextualMusicHandler;
import com.farcr.nomansland.client.particle.*;
import com.farcr.nomansland.client.renderer.SunDogRenderer;
import com.farcr.nomansland.client.renderer.UpperAtmosphericRenderer;
import com.farcr.nomansland.client.renderer.entity.*;
import com.farcr.nomansland.client.renderer.dreams.MoonlightDreamRenderer;
import com.farcr.nomansland.client.renderer.rendertype.MoonlightGlowRenderType;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.nirvana.NirvanaIntegration;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.io.IOException;

@EventBusSubscriber(modid = NoMansLand.MODID, value = Dist.CLIENT)
public class ClientSetupEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        AmbienceHandler.FOG_MODIFIER_HANDLER.fillFogModifiers();
        ContextualMusicHandler.buildMusicContext();

        event.enqueueWork(() -> {
            ItemProperties.register(NMLItems.BANDAGE.get(), NoMansLand.location("has_potion"),
                    (stack, level, entity, seed) -> {
                        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
                        if (potionContents != null && potionContents.getAllEffects().iterator().hasNext()) {
                            return 1.0F;
                        }
                        return 0.0F;
                    });
        });
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/firebomb")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/ink_bomb")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/explosive")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/living_urn")));
        if (Mods.NIRVANA.isLoaded()) event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/fat_joint")));

        //Load all the pot models here otherwise you die
        for (int i = 0; i < 6; i++) {
            var path = "block/ancient_pots/ancient_pot_small_" + (i+1);
            event.register(ModelResourceLocation.standalone(NoMansLand.location(path)));
        }
        for (int i = 0; i < 1; i++) {
            var path = "block/ancient_pots/ancient_pot_large_" + (i+1);
            event.register(ModelResourceLocation.standalone(NoMansLand.location(path)));
        }
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NMLEntities.BILLHOOK_BASS.get(), BillhookBassRenderer::new);

        event.registerEntityRenderer(NMLEntities.DEER.get(), DeerRenderer::new);
        event.registerEntityRenderer(NMLEntities.GOOSE.get(), GooseRenderer::new);
        event.registerEntityRenderer(NMLEntities.MOOSE.get(), MooseRenderer::new);
        event.registerEntityRenderer(NMLEntities.TORTOISE.get(), TortoiseRenderer::new);
        event.registerEntityRenderer(NMLEntities.BUDDY.get(), BuddyRenderer::new);

        event.registerEntityRenderer(NMLEntities.DREAMING_PLAYER.get(), DreamingPlayerRenderer::new);

        event.registerEntityRenderer(NMLEntities.FIREBOMB.get(), FirebombRenderer::new);
        event.registerEntityRenderer(NMLEntities.INK_BOMB.get(), InkBombRenderer::new);
        event.registerEntityRenderer(NMLEntities.EXPLOSIVE.get(), ExplosiveRenderer::new);
        event.registerEntityRenderer(NMLEntities.LIVING_URN.get(), LivingUrnRenderer::new);
        if (Mods.NIRVANA.isLoaded()) event.registerEntityRenderer(NirvanaIntegration.FAT_JOINT.get(), FatJointRenderer::new);

        event.registerEntityRenderer(NMLEntities.INCENDIARY_ARROW.get(), IncendiaryArrowRenderer::new);
        event.registerEntityRenderer(NMLEntities.EMBER.get(), NoopRenderer::new);

        event.registerEntityRenderer(NMLEntities.LINGERING_CLOUD.get(), NoopRenderer::new);
        event.registerEntityRenderer(NMLEntities.INK_CLOUD.get(), NoopRenderer::new);
        event.registerEntityRenderer(NMLEntities.PACIFIED_CLOUD.get(), NoopRenderer::new);

        event.registerBlockEntityRenderer(NMLBlockEntities.POT.get(), PotRenderer::new);
        event.registerEntityRenderer(NMLEntities.LIVING_POT.get(), LivingPotRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        NMLModelLayers.registerLayers(event);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        NMLArmorModels.addLayers(event);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        NMLClientExtensions.registerClientExtensions(event);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NMLParticleTypes.PALE_CHERRY_LEAVES.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FallingParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.CAVE_DUST.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new CaveDustParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.RESIN_DROPLET.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidFallingParticle(clientLevel, d, e, f, sprites, NMLParticleTypes.RESIN_DROPLET_FLAT));
        event.registerSpriteSet(NMLParticleTypes.RESIN_DROPLET_FLAT.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidLandParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.MAPLE_SYRUP_DROPLET.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidFallingParticle(clientLevel, d, e, f, sprites, NMLParticleTypes.MAPLE_SYRUP_DROPLET_FLAT));
        event.registerSpriteSet(NMLParticleTypes.MAPLE_SYRUP_DROPLET_FLAT.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidLandParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.OIL.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidFallingParticle(clientLevel, d, e, f, sprites, NMLParticleTypes.OIL_FLAT));
        event.registerSpriteSet(NMLParticleTypes.OIL_SPLASH.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidSplashingParticle(clientLevel, d, e, f, g, h, i, sprites, NMLParticleTypes.OIL_FLAT));
        event.registerSpriteSet(NMLParticleTypes.OIL_FLAT.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidLandParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.RESIN_OIL_BUBBLE.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new BubbleParticle(clientLevel, d, e, f, g, h, i, sprites, NMLParticleTypes.RESIN_OIL_BUBBLE_POP));
        event.registerSpriteSet(NMLParticleTypes.RESIN_OIL_BUBBLE_POP.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new BubblePopParticle(clientLevel, d, e, f, g, h, i, sprites));
        event.registerSpriteSet(NMLParticleTypes.SCULK_AMBIENCE.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new SculkAmbienceParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.MALEVOLENT_EMBERS.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new EmbersParticle(clientLevel, d, e, f, g, h, i, sprites));
        event.registerSpriteSet(NMLParticleTypes.MALEVOLENT_FLAME.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FlameParticle(clientLevel, d, e, f, g, h, i, sprites));
        event.registerSpriteSet(NMLParticleTypes.MILK_DROPLET.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidFallingParticle(clientLevel, d, e, f, sprites, NMLParticleTypes.MILK_DROPLET_FLAT));
        event.registerSpriteSet(NMLParticleTypes.MILK_DROPLET_FLAT.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidLandParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.TRANSLUCENT_DUST.get(), sprites
                -> (translucentDustParticleOptions, clientLevel, d, e, f, g, h, i)
                -> new TranslucentDustParticle(clientLevel, d, e, f, g, h, i, translucentDustParticleOptions, sprites));
        event.registerSpecial(NMLParticleTypes.MOONLIGHT_RAY.get(), (type, clientLevel, d, e, f, g, h, i)
            -> new MoonlightRayParticle(clientLevel, d, e, f, g, h, i));
        event.registerSpriteSet(NMLParticleTypes.MOONLIGHT_FLAME.get(), sprites
            -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
            -> new FlameParticle(clientLevel, d, e, f, g, h, i, sprites));
        event.registerSpriteSet(NMLParticleTypes.MOONLIGHT_SPARK.get(), sprites
            -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
            -> new MoonlightSparkParticle(clientLevel, d, e, f, g, h, i, sprites));
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
            new ShaderInstance(
                event.getResourceProvider(),
                NoMansLand.location("rendertype_moonlight"),
                DefaultVertexFormat.NEW_ENTITY
            ),
            shader -> MoonlightRayParticle.MOONLIGHT_RENDER_SHADER = shader
        );
        event.registerShader(
            new ShaderInstance(
                event.getResourceProvider(),
                NoMansLand.location("rendertype_moonlight_glow"),
                DefaultVertexFormat.POSITION_TEX
            ),
            shader -> MoonlightGlowRenderType.MOONLIGHT_GLOW_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        NoMansLand.location("sun_dog"),
                        DefaultVertexFormat.POSITION_TEX_COLOR
                ),
                shader -> SunDogRenderer.SUN_DOG_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        NoMansLand.location("upper_atmosphere"),
                        DefaultVertexFormat.POSITION_COLOR
                ),
                shader -> UpperAtmosphericRenderer.UPPER_ATMOSPHERE_SHADER = shader
        );
        event.registerShader(
            new ShaderInstance(
                event.getResourceProvider(),
                NoMansLand.location("friend_moon_dream"),
                DefaultVertexFormat.POSITION_COLOR
            ),
            shader -> MoonlightDreamRenderer.DREAM_SKY_SHADER = shader
        );
    }
}
