package com.farcr.nomansland.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/*
    @Author: SnowyStarfall
    (I [Liz] asked her for help with the custom rendering math)
 */
public class MoonlightRayParticle extends SingleQuadParticle
{
    public static ShaderInstance MOONLIGHT_RENDER_SHADER;
    public static final ParticleRenderType MOONLIGHT_RAY = new ParticleRenderType()
    {
        public static float shaderTime;

        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager)
        {
            Minecraft minecraft = Minecraft.getInstance();
            shaderTime += minecraft.getTimer().getGameTimeDeltaTicks() / 1000.0f;

            RenderSystem.setShader(() -> MOONLIGHT_RENDER_SHADER);
            MOONLIGHT_RENDER_SHADER.safeGetUniform("ElapsedTime").set(shaderTime);
            RenderSystem.depthMask(false);
//			RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.enableBlend();
//			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
//			RenderSystem.disableCull();
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        }

        @Override
        public String toString()
        {
            return "MOONLIGHT_RAY";
        }
    };

    private float shaderOffset;

    public MoonlightRayParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
    {
        super(level, x, y, z, 0, 0, 0);

        this.lifetime = 240 + level.random.nextIntBetweenInclusive(-120, 120);
        this.hasPhysics = false;
        this.gravity = 0.0f;

        shaderOffset = level.random.nextFloat() * 16f;
    }

    @Override
    public void tick()
    {
        if (this.age++ >= this.lifetime)
        {
            this.remove();
            return;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Vector3f camPos = camera.getPosition().toVector3f();
        Vector3f rayToCam = new Vector3f();
        camPos.sub(new Vector3f((float) x, (float) y, (float) z), rayToCam);
        float moonAngle = (float) ((minecraft.level.getTimeOfDay(partialTicks) * Math.TAU) + Math.PI / 2.0);

        Vector3f moonNormal = new Vector3f((float) Math.cos(moonAngle), (float) Math.sin(moonAngle), 0);

//		// Flatten points to plane
//		Vec3 particlePosition = new Vec3(x, y, z);
//		Vec3 pointToCameraVec3 = camera.getPosition().subtract(particlePosition);
//		Vector3f pointToCamera = new Vector3f((float) pointToCameraVec3.x, (float) pointToCameraVec3.y, (float) pointToCameraVec3.z);
//		Vector3f flattenedPointToCamera = flattenVectorToPlane(pointToCamera, moonNormal);
//
//		// Calculate angle between points
//		Vector3f referenceDirection = moonNormal.cross(new Vector3f(0, 1, 0));
//		if (referenceDirection.lengthSquared() == 0)
//		{
//			referenceDirection = moonNormal.cross(new Vector3f(1, 0, 0));
//		}
//		referenceDirection.normalize();
//
//		Vector3f cross = referenceDirection.cross(flattenedPointToCamera);
//		float dot = referenceDirection.dot(flattenedPointToCamera);
//		float angle = (float) Math.atan2(cross.dot(moonNormal), dot);

        float width = 3f;
        float height = 16f;
        float alpha = (float) Math.clamp(Math.sin((double) age / (double) lifetime * Math.PI) * 3f, 0f, 1f);

        PoseStack poseStack = new PoseStack();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        poseStack.mulPose(new Matrix4f().billboardCylindrical(
            new Vector3f((float) x, (float) y - (height / 2f), (float) z),
            new Vector3f((float) camPos.x, (float) camPos.y, (float) camPos.z),
            moonNormal
        ));

        PoseStack.Pose pose = poseStack.last();

        buffer.addVertex(pose, -width, -height, 0f)
            .setColor(shaderOffset, 0f, 0f, alpha)
            .setUv(0, 0).setUv1(0, 0).setUv2(0, 0)
            .setNormal(0f, 0f, 1f);

        buffer.addVertex(pose, width, -height, 0f)
            .setColor(shaderOffset, 0f, 0f, alpha)
            .setUv(1, 0).setUv1(0, 0).setUv2(0, 0)
            .setNormal(0f, 0f, 1f);

        buffer.addVertex(pose, width, height, 0f)
            .setColor(shaderOffset, 0f, 0f, alpha)
            .setUv(1, 1).setUv1(0, 0).setUv2(0, 0)
            .setNormal(0f, 0f, 1f);

        buffer.addVertex(pose, -width, height, 0f)
            .setColor(shaderOffset, 0f, 0f, alpha)
            .setUv(0, 1).setUv1(0, 0).setUv2(0, 0)
            .setNormal(0f, 0f, 1f);

//		float xx = matrix.m00() * -width + matrix.m10() * 0 + matrix.m20() * 0;
//		float yy = matrix.m01() * -width + matrix.m11() * 0 + matrix.m21() * 0;
//		float zz = matrix.m02() * -width + matrix.m12() * 0 + matrix.m22() * 0;
//		Vector4f translated = matrix.transform(new Vector4f(new Vector3f(-width, 0, 0), 1));
//		NoMansLand.LOGGER.debug("Ray " + (float) translated.x + " " + translated.y + " " + translated.z);
    }

    private Vector3f flattenVectorToPlane(Vector3f vector, Vector3f planeNormal)
    {
        Vector3f result = vector.sub(planeNormal.mul(vector.dot(planeNormal)));

        // Prevent NaN
        if (result.lengthSquared() > 0)
        {
            result.normalize();
        }
        else
        {
            result = new Vector3f();
        }

        return result;
    }

    @Override
    protected float getU0()
    {
        return 0;
    }

    @Override
    protected float getU1()
    {
        return 1;
    }

    @Override
    protected float getV0()
    {
        return 0;
    }

    @Override
    protected float getV1()
    {
        return 1;
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return MOONLIGHT_RAY;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<MoonlightRayParticleOptions>
    {
        public Particle createParticle(MoonlightRayParticleOptions type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            return new MoonlightRayParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}