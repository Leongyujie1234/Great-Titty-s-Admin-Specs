package com.adminspec.client;

import com.adminspec.entity.GiantHandEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Giant Hand entity. The hand has no model — its presence is communicated
 * purely through server-spawned particles (CAMPFIRE_COSY_SMOKE / EXPLOSION on impact).
 * The renderer exists only to provide a valid display texture; it draws nothing extra.
 */
public class GiantHandRenderer extends EntityRenderer<GiantHandEntity> {

    private static final ResourceLocation NO_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("adminspec", "textures/entity/empty.png");

    public GiantHandRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(GiantHandEntity entity) {
        return NO_TEXTURE;
    }

    @Override
    public boolean shouldRender(GiantHandEntity entity, Frustum frustum, double x, double y, double z) {
        // The hand has no visual model — don't render anything.
        // Particles are spawned by the entity's tick() on the server and replicated to clients.
        return false;
    }
}
