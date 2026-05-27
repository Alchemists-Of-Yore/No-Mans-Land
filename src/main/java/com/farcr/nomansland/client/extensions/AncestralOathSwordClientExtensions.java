package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.client.renderer.effect.AccumulateZoomRenderer;
import com.farcr.nomansland.common.item.AncestralOathSwordItem;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class AncestralOathSwordClientExtensions implements IClientItemExtensions {

    Minecraft minecraft = Minecraft.getInstance();
    public static AccumulateZoomRenderer TRAIL_INSTANCE = new AccumulateZoomRenderer();
    static TextureTarget renderTarget = new TextureTarget(100, 100, false, false);
    public static RenderTarget getRenderTarget() { return renderTarget; }

    private void resetValues() {
        animateTime = 0f;
        glintAnimateTime = 0f;
    }

    public void render(float partialTicks) {
        // go here because the method below doesnt even run in anything but first person
        if (!minecraft.options.getCameraType().isFirstPerson()) {
            resetValues();
            return;
        }

        if (glintAnimateTime <= 0f) {
            TRAIL_INSTANCE.persistentTarget.forceClear(false);
            renderTarget.clear(false);
            return;
        }
        Window window = minecraft.getWindow();
        if (renderTarget.width != window.getWidth() || renderTarget.height != window.getHeight())
            renderTarget.resize(window.getWidth(), window.getHeight(), false);

        renderTarget.setClearColor(0f, 0f, 0f, 0f);
        renderTarget.bindWrite(false);
        renderTarget.clear(false);
        renderTarget.bindWrite(false);

        renderSwordToTarget(partialTicks);

        TRAIL_INSTANCE.renderConstant(
            this.minecraft, partialTicks,
            1.0f, 1F - (glintAnimateTime / MAX_GLINT_ANIMATE) / 100
        );
        drawRenderTarget(renderTarget, (glintAnimateTime / MAX_GLINT_ANIMATE));
    }

    // ough
    private void drawRenderTarget(RenderTarget target, float alpha) {
        this.minecraft.getMainRenderTarget().bindWrite(true);

        Matrix4f lastProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting lastSorting = RenderSystem.getVertexSorting();
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();

        modelViewStack.pushMatrix();
        modelViewStack.identity();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, target.getColorTextureId());

        BufferBuilder buffer = RenderSystem.renderThreadTesselator()
            .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        buffer.addVertex(-1f, -1f, 0f)
            .setUv(0f, 0f).setColor(1f, 1f, 1f, alpha);
        buffer.addVertex( 1f, -1f, 0f)
            .setUv(1f, 0f).setColor(1f, 1f, 1f, alpha);
        buffer.addVertex( 1f,  1f, 0f)
            .setUv(1f, 1f).setColor(1f, 1f, 1f, alpha);
        buffer.addVertex(-1f,  1f, 0f)
            .setUv(0f, 1f).setColor(1f, 1f, 1f, alpha);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.setProjectionMatrix(lastProjection, lastSorting);
        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    private void renderSwordToTarget(float partialTicks) {
        PoseStack poseStack = new PoseStack();
        HumanoidArm arm = this.minecraft.player.getMainArm();

        float[] shaderColor = RenderSystem.getShaderColor().clone();
        float opacity = 0.25f;
//        RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], 1F - (animateTime / MAX_ANIMATE_TIME) * 1.15F);

        boolean rightHanded = arm == HumanoidArm.RIGHT;
        ItemInHandRenderer itemInHandRenderer = this.minecraft.gameRenderer.itemInHandRenderer;
        itemInHandRenderer.applyItemArmTransform(poseStack, arm, (animateTime / MAX_ANIMATE_TIME) / 2f);
        itemInHandRenderer.applyItemArmAttackTransform(poseStack, arm,
            1F - (animateTime / MAX_ANIMATE_TIME) * (animateTime / MAX_ANIMATE_TIME)
        );
        int packedLight = this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        itemInHandRenderer.renderItem(
            this.minecraft.player, this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND),
            rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
            !rightHanded, poseStack, bufferSource, packedLight
        );
        bufferSource.endBatch();

//        RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
    }

    /*
    * Probably not good practice to reimplement this, but
    * I want to give the sword a cool shake animation so fuuuuuuuck
    */
    @Override
    public boolean applyForgeHandTransform(
        @NotNull PoseStack poseStack, @NotNull LocalPlayer player,
        @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand,
        float partialTick, float equippedProgress, float swingProgress
    ) {
        if (equippedProgress >= 1f - partialTick) resetValues();

        ItemInHandRenderer itemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;
        InteractionHand hand = player.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        boolean rightHanded = arm == HumanoidArm.RIGHT;
        int invert = rightHanded ? 1 : -1;
        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
            itemInHandRenderer.applyItemArmTransform(poseStack, arm, equippedProgress);
            poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) invert * 13.365F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) invert * 78.05F));
        } else {
            float f5 = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
            float f6 = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * (float) (Math.PI * 2));
            float f10 = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
            poseStack.translate((float) invert * f5, f6, f10);
//            if (glintAnimateTime > 0.0f) {
//                int divisions = 36;
//                float totalProgress = 1f - (animateTime / MAX_ANIMATE_TIME);
//                for (int i = 0; i < Math.floor(totalProgress * divisions); i++) {
//                    poseStack.pushPose();
//                    float fakeProgress = (float) i / divisions;
//                    itemInHandRenderer.applyItemArmTransform(poseStack, arm, equippedProgress);
//                    itemInHandRenderer.applyItemArmAttackTransform(poseStack, arm, fakeProgress);
//                    int packedLight = this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTick);
//
//                    float[] shaderColor = RenderSystem.getShaderColor().clone();
//                    RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], 0.01f);
//                    itemInHandRenderer.renderItem(
//                        player, itemInHand,
//                        rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
//                            : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !rightHanded,
//                        poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), packedLight
//                    );
//
//                    RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
//                    poseStack.popPose();
//                }
//            }
            itemInHandRenderer.applyItemArmTransform(poseStack, arm, equippedProgress);
            itemInHandRenderer.applyItemArmAttackTransform(poseStack, arm, swingProgress);
        }
        if (!minecraft.isPaused()) {
            animateTime = Math.max(animateTime - partialTick, 0f);
            glintAnimateTime = Math.max(glintAnimateTime - partialTick, 0f);
        }
        poseStack.translate(getShakePosition(animateTime) / 200f, 0f, 0f);

        return true;
    }

    // its more artistic if I do it like this (lazy
    private static final float[] SHAKE_ARRAY = new float[]{
        3, -3, 2, -1.75f, -2, 1.75f,
        -1.5f, -1.25f, -1, 1.5f, 1.25f,
        1, -.75f, -.66f, -.5f, -.33f,
        .75f, .66f, .5f, .33f, 0
    };
    private float getShakePosition(float animateTime) {
        return SHAKE_ARRAY[(int) ((SHAKE_ARRAY.length - 1)
            * ((MAX_ANIMATE_TIME - animateTime) / MAX_ANIMATE_TIME))];
    }

    public static final float MAX_ANIMATE_TIME = 30f;
    public float animateTime = 0f;

    public static final float MAX_GLINT_ANIMATE = 90f;
    public float glintAnimateTime = 0f;

    public boolean clientCancelAttack(AncestralOathSwordItem ancestralOathSword, Entity entity) {
        if (!AncestralOathSwordItem.canHurtUnderOath(entity)) {
            Minecraft.getInstance().getConnection().setActionBarText(
                new ClientboundSetActionBarTextPacket(Component.translatable("item.nomansland.ancestral_oath_sword.refuse"))
            );
            // do cool shake and glint glow here lol
            glintAnimateTime = MAX_GLINT_ANIMATE;
            animateTime = MAX_ANIMATE_TIME;
            return true;
        }
        return false;
    }
}
