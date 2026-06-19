package com.adminspec.client;

import com.adminspec.client.ClientBeamManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid="adminspec", bus=EventBusSubscriber.Bus.GAME, value={Dist.CLIENT})
public final class BeamRenderHandler {
    private static final float BASE_HALF_WIDTH = 0.4f;
    private static final float BASE_ALPHA = 0.8f;
    private static final float COLOR_R = 1.0f;
    private static final float COLOR_G = 1.0f;
    private static final float COLOR_B = 0.95f;

    private BeamRenderHandler() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        List<ClientBeamManager.Beam> beams = ClientBeamManager.getBeams();
        if (beams.isEmpty()) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        DeltaTracker delta = event.getPartialTick();
        float partialTick = delta.getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        for (ClientBeamManager.Beam beam : beams) {
            float progress = ((float)beam.tickCounter + partialTick) / (float)beam.maxLifetime;
            BeamRenderHandler.renderBeam(pose, consumer, camera, beam, progress);
        }
    }

    private static void renderBeam(PoseStack pose, VertexConsumer consumer, Vec3 camera, ClientBeamManager.Beam beam, float progress) {
        Vec3 worldUp;
        progress = Math.min(1.0f, Math.max(0.0f, progress));
        float halfWidth = 0.4f * (1.0f - progress);
        float alpha = 0.8f * (1.0f - progress);
        if (halfWidth <= 0.001f || alpha <= 0.001f) {
            return;
        }
        Vec3 end = new Vec3(beam.endX - camera.x, beam.endY - camera.y, beam.endZ - camera.z);
        Vec3 start = new Vec3(beam.startX - camera.x, beam.startY - camera.y, beam.startZ - camera.z);
        double length = end.subtract(start).length();
        if (length < 0.01) {
            return;
        }
        Vec3 forward = end.subtract(start).normalize();
        Vec3 right = forward.cross(worldUp = new Vec3(0.0, 1.0, 0.0));
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        }
        right = right.normalize();
        Vec3 up = right.cross(forward).normalize();
        Matrix4f matrix = pose.last().pose();
        Vec3 sr = right.scale((double)halfWidth);
        Vec3 su = up.scale((double)halfWidth);
        Vec3 sTL = start.add(su).subtract(sr);
        Vec3 sTR = start.add(su).add(sr);
        Vec3 sBR = start.subtract(su).add(sr);
        Vec3 sBL = start.subtract(su).subtract(sr);
        Vec3 eTL = end.add(su).subtract(sr);
        Vec3 eTR = end.add(su).add(sr);
        Vec3 eBR = end.subtract(su).add(sr);
        Vec3 eBL = end.subtract(su).subtract(sr);
        BeamRenderHandler.quad(consumer, matrix, sTL, sTR, eTR, eTL, alpha);
        BeamRenderHandler.quad(consumer, matrix, sBL, sBR, eBR, eBL, alpha);
        BeamRenderHandler.quad(consumer, matrix, sTR, sBR, eBR, eTR, alpha);
        BeamRenderHandler.quad(consumer, matrix, sBL, sTL, eTL, eBL, alpha);
    }

    private static void quad(VertexConsumer c, Matrix4f matrix, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float alpha) {
        c.addVertex(matrix, (float)p0.x, (float)p0.y, (float)p0.z).setColor(1.0f, 1.0f, 0.95f, alpha);
        c.addVertex(matrix, (float)p1.x, (float)p1.y, (float)p1.z).setColor(1.0f, 1.0f, 0.95f, alpha);
        c.addVertex(matrix, (float)p2.x, (float)p2.y, (float)p2.z).setColor(1.0f, 1.0f, 0.95f, alpha);
        c.addVertex(matrix, (float)p3.x, (float)p3.y, (float)p3.z).setColor(1.0f, 1.0f, 0.95f, alpha);
    }
}
