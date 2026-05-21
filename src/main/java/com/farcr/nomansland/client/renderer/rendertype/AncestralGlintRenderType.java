package com.farcr.nomansland.client.renderer.rendertype;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.friend.condition.MoonlightOfferingConditions;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class AncestralGlintRenderType {
    public static final RenderType ANCESTRAL_GLINT = RenderType.create(
        "ancestral_glint", DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS, 1536, RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
            .setTextureState(
                new RenderStateShard.TextureStateShard(
                    NoMansLand.location("textures/misc/ancestral_glint.png"), true, false)
            )
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
            .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
            .setTexturingState(RenderStateShard.GLINT_TEXTURING)
            .createCompositeState(false)
    );

    public static ItemStack itemContext;
    public static void setContext(ItemStack newContext) {
        itemContext = newContext;
    }

    public static boolean renderGlintCondition() {
        if (itemContext != null) {
            boolean validItem = itemContext.is(NMLItems.ANCESTRAL_OATH_SWORD);
            itemContext = null;
            return validItem;
        }
        return false;
    }

    public static void addGlint(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map) {
        if (!map.containsKey(ANCESTRAL_GLINT))
            map.put(ANCESTRAL_GLINT, new ByteBufferBuilder(ANCESTRAL_GLINT.bufferSize()));
    }

    public static VertexConsumer getConsumer(
        MultiBufferSource bufferSource, VertexConsumer originalConsumer
    ) {
        if (renderGlintCondition()) {
            return VertexMultiConsumer.create(
                bufferSource.getBuffer(ANCESTRAL_GLINT),
                originalConsumer
            );
        }
        return originalConsumer;
    }
}
