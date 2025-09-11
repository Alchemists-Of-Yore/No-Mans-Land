package com.farcr.nomansland.client.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.ambience.AmbienceHandler;
import com.farcr.nomansland.client.model.AncientBronzeMaskModel;
import com.farcr.nomansland.client.model.BillhookBassModel;
import com.farcr.nomansland.client.model.BuriedModel;
import com.farcr.nomansland.client.model.GooseModel;
import com.farcr.nomansland.client.model.deer.DeerModel;
import com.farcr.nomansland.client.model.moose.MooseModel;
import com.farcr.nomansland.client.model.tortoise.TortoiseModel;
import com.farcr.nomansland.client.model.tortoise.TortoiseShellModel;
import com.farcr.nomansland.client.particle.*;
import com.farcr.nomansland.client.renderer.*;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.nirvana.NirvanaIntegration;
import com.farcr.nomansland.common.registry.NMLFluids;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = NoMansLand.MODID, value = Dist.CLIENT)
public class ClientSetupEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        AmbienceHandler.FOG_MODIFIER_HANDLER.fillFogModifiers();
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/firebomb")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/ink_bomb")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/explosive")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/living_urn")));
        if (Mods.NIRVANA.isLoaded()) event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/fat_joint")));
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NMLEntities.BILLHOOK_BASS.get(), BillhookBassRenderer::new);

        event.registerEntityRenderer(NMLEntities.DEER.get(), DeerRenderer::new);
        event.registerEntityRenderer(NMLEntities.GOOSE.get(), GooseRenderer::new);
        event.registerEntityRenderer(NMLEntities.MOOSE.get(), MooseRenderer::new);
        event.registerEntityRenderer(NMLEntities.TORTOISE.get(), TortoiseRenderer::new);

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
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(NMLModelLayers.MOOSE_LAYER, MooseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.BURIED_LAYER, BuriedModel::createBodyLayer);

        event.registerLayerDefinition(NMLModelLayers.BASS_LAYER, BillhookBassModel::createBodyLayer);

        event.registerLayerDefinition(NMLModelLayers.DEER_LAYER, DeerModel::createBodyLayer);

        event.registerLayerDefinition(NMLModelLayers.GOOSE_LAYER, GooseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.TORTOISE_LAYER, TortoiseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.TORTOISE_SHELL_LAYER, TortoiseShellModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.ANCIENT_BRONZE_MASK_LAYER, AncientBronzeMaskModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL_RESIN_OIL = NoMansLand.location("block/fluid/resin_oil");
            private static final ResourceLocation FLOWING_RESIN_OIL = NoMansLand.location("block/fluid/flowing_resin_oil");

            public ResourceLocation getStillTexture() {
                return STILL_RESIN_OIL;
            }

            public ResourceLocation getFlowingTexture() {
                return FLOWING_RESIN_OIL;
            }
        }, NMLFluids.RESIN_OIL_TYPE.get());

        event.registerItem(new IClientItemExtensions() {
            public Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return new TortoiseShellModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(NMLModelLayers.TORTOISE_SHELL_LAYER));
            }

            public void setupModelAnimations(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, Model model, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                TortoiseShellModel<?> tortoiseShellModel = (TortoiseShellModel<?>) model;
                if (livingEntity.isCrouching()) {
                    tortoiseShellModel.tortoiseShell.xRot = 0.5F;
                    tortoiseShellModel.tortoiseShell.z = 10.5F;
                }
            }
        }, NMLItems.TORTOISE_SHELL.get());

        event.registerItem(new IClientItemExtensions() {
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> original) {
                if (slot != EquipmentSlot.HEAD) return original;

                ModelPart baked = Minecraft.getInstance().getEntityModels().bakeLayer(NMLModelLayers.ANCIENT_BRONZE_MASK_LAYER);
                HumanoidModel<?> model = new AncientBronzeMaskModel<>(baked);

                ClientHooks.copyModelProperties(original, model);
                return model;
            }
        }, NMLItems.ANCIENT_BRONZE_MASK.get());
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
    }
}
