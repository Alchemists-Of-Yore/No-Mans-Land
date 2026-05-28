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
import net.minecraft.world.entity.LivingEntity;
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

    public float getGlintOpacity(ItemStack itemStack, LivingEntity livingEntity) {
        float baseOpacity = (glintAnimateTime / MAX_GLINT_ANIMATE);
        if (livingEntity.getUseItem().equals(itemStack)
        && itemStack.getItem() instanceof AncestralOathSwordItem oathSword) {
            // the first excuse I have to use more than 2 parameters in a max function and I learn java doesnt allow it ???
            baseOpacity = Math.max(Math.max(baseOpacity,
                (AncestralOathSwordItem.SWORD_PARRY_TICKS
                    - oathSword.getParryTiming(itemStack, livingEntity))
                    / AncestralOathSwordItem.SWORD_PARRY_TICKS
            ), 0f);
        }
        return baseOpacity;
    }

    public void render(float partialTick) {
        // go here because the method below doesnt even run in anything but first person
        if (!minecraft.options.getCameraType().isFirstPerson()) {
            resetValues();
            return;
        }

        if (!minecraft.isPaused()) {
            animateTime = Math.max(animateTime - partialTick, 0f);
            glintAnimateTime = Math.max(glintAnimateTime - partialTick, 0f);
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
        renderTarget.clear(false);
        renderTarget.bindWrite(false);

        renderSwordToTarget(partialTick);

        TRAIL_INSTANCE.renderConstant(
            this.minecraft, partialTick,
            1.0f, 1F - (glintAnimateTime / MAX_GLINT_ANIMATE) / 100f
        );
        drawRenderTarget(renderTarget, (glintAnimateTime / MAX_GLINT_ANIMATE) * .5f);
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

        minecraft.gameRenderer.bobHurt(poseStack, partialTicks);
        if (minecraft.options.bobView().get())
            minecraft.gameRenderer.bobView(poseStack, partialTicks);

        HumanoidArm arm = this.minecraft.player.getMainArm();

        boolean rightHanded = arm == HumanoidArm.RIGHT;
        ItemInHandRenderer itemInHandRenderer = this.minecraft.gameRenderer.itemInHandRenderer;
        float time = (glintAnimateTime / MAX_GLINT_ANIMATE);
        poseStack.translate(0f, (1f - time) / 16f, 0f);
        float poseScale = 1.0f - (time * 0.05f);
        poseStack.scale(poseScale, poseScale, poseScale);

        ItemStack itemStack = this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
//        itemInHandRenderer.applyItemArmTransform(poseStack, arm, 0f);
        applyForgeHandTransform(
            poseStack, this.minecraft.player, arm, itemStack,
            partialTicks, 0f, 0f
        );

        int packedLight = this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        itemInHandRenderer.renderItem(
            this.minecraft.player, itemStack,
            rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
            !rightHanded, poseStack, bufferSource, packedLight
        );
        bufferSource.endBatch();
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

            itemInHandRenderer.applyItemArmTransform(poseStack, arm, equippedProgress);
            itemInHandRenderer.applyItemArmAttackTransform(poseStack, arm, swingProgress);
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

    public static float MAX_ANIMATE_TIME = 7f;
    public float animateTime = 0f;

    public static float MAX_GLINT_ANIMATE = 45f;
    public float glintAnimateTime = 0f;

    public void parrySuccessful() {
        glintAnimateTime = MAX_GLINT_ANIMATE;
    }

    public boolean clientCancelAttack(AncestralOathSwordItem ancestralOathSword, Entity entity) {
        if (!AncestralOathSwordItem.canHurtUnderOath(entity)) {
            Minecraft.getInstance().getConnection().setActionBarText(
                new ClientboundSetActionBarTextPacket(Component.translatable("item.nomansland.ancestral_oath_sword.refuse"))
            );
            // do cool shake and glint glow here lol
            MAX_ANIMATE_TIME = 7f;
            glintAnimateTime = MAX_GLINT_ANIMATE;
            animateTime = MAX_ANIMATE_TIME;
            return true;
        }
        return false;
    }
}
