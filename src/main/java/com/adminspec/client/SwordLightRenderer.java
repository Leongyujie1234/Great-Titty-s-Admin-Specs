package com.adminspec.client;

import com.adminspec.entity.SwordLightEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Sword Light beam. The beam is "instantaneous, beyond the eye can see"
 * so it has no persistent visual — its travel is communicated via END_ROD particles spawned
 * in tick(). This renderer returns false from shouldRender so nothing extra is drawn.
 */
public class SwordLightRenderer extends EntityRenderer<SwordLightEntity> {

    private static final ResourceLocation NO_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("adminspec", "textures/entity/empty.png");

    public SwordLightRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(SwordLightEntity entity) {
        return NO_TEXTURE;
    }

    @Override
    public boolean shouldRender(SwordLightEntity entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    public void render(SwordLightEntity entity, float yaw, float partialTicks, PoseStack pose,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int packedLight) {
        // No-op — visuals are purely from server-spawned particles.
    }
}
