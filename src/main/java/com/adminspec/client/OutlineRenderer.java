package com.adminspec.client;

import com.adminspec.AdminSpecMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * Renders a colored "glow shell" around the local player when Reverse Flow or Emperor Yama is active.
 *  - Reverse Flow Protection Seal: BLUE shell (color 0x3070FF)
 *  - Emperor Yama:                 BLACK shell (color 0x000000)
 *
 * The shell is a translucent box drawn just larger than the player.
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = AdminSpecMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class OutlineRenderer {

    private OutlineRenderer() {}

    private static final int COLOR_REVERSE_FLOW = 0x3070FF;
    private static final int COLOR_EMPEROR_YAMA = 0x000000;

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        // Only render outline on the local player.
        if (!(player instanceof LocalPlayer)) return;

        ClientSpecState.Snapshot snap = ClientSpecState.get(player);
        if (snap == null) return;
        if (!snap.reverseFlowActive && !snap.yamaActive) return;

        int color = snap.yamaActive ? COLOR_EMPEROR_YAMA : COLOR_REVERSE_FLOW;
        drawGlowShell(event.getPoseStack(), event.getMultiBufferSource(), color);
    }

    private static void drawGlowShell(PoseStack pose, MultiBufferSource buffers, int color) {
        float a = 0.35f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        VertexConsumer consumer = buffers.getBuffer(RenderType.translucent());
        var matrix = pose.last().pose();

        float s = 0.65f;
        float h = 1.0f;
        float x0 = -s, x1 = s;
        float y0 = 0f, y1 = 2 * h;
        float z0 = -s, z1 = s;

        // 1.21 VertexConsumer API: vertex(matrix, x, y, z) -> color -> normal -> endVertex.
        // Some 1.21 mappings renamed .vertex to .addVertex, but NeoForge 1.21.1 keeps .vertex
        // with a new (matrix, x, y, z) signature that returns VertexConsumer for chaining.
        quad(consumer, matrix, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, r, g, b, a);
        quad(consumer, matrix, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, r, g, b, a);
        quad(consumer, matrix, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, r, g, b, a);
        quad(consumer, matrix, x1, y0, z1, x1, y1, z1, x1, y1, z0, x1, y0, z0, r, g, b, a);
        quad(consumer, matrix, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, r, g, b, a);
        quad(consumer, matrix, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, r, g, b, a);
    }

    private static void quad(
            VertexConsumer c,
            org.joml.Matrix4f matrix,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float r, float g, float b, float a) {

        // 1.21.1 VertexConsumer API: addVertex(x, y, z) returns VertexConsumer, then setColor.
        // We apply the matrix transform manually since there's no vertex(Matrix4f, ...) overload.
        org.joml.Vector3f v = new org.joml.Vector3f();
        matrix.transformPosition(x0, y0, z0, v); c.addVertex(v.x, v.y, v.z).setColor(r, g, b, a);
        matrix.transformPosition(x1, y1, z1, v); c.addVertex(v.x, v.y, v.z).setColor(r, g, b, a);
        matrix.transformPosition(x2, y2, z2, v); c.addVertex(v.x, v.y, v.z).setColor(r, g, b, a);
        matrix.transformPosition(x3, y3, z3, v); c.addVertex(v.x, v.y, v.z).setColor(r, g, b, a);
    }
}
