package com.adminspec.client.renderer;

import com.adminspec.entity.TheWorldStandEntity;
import mod.azure.azurelib.common.api.client.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TheWorldStandRenderer extends GeoEntityRenderer<TheWorldStandEntity> {
    public TheWorldStandRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TheWorldStandModel());
    }
}
