package com.adminspec.client.renderer;

import com.adminspec.entity.TheWorldStandEntity;
import mod.azure.azurelib.common.api.client.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

public class TheWorldStandModel extends GeoModel<TheWorldStandEntity> {
    @Override
    public ResourceLocation getModelResource(TheWorldStandEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("adminspec", "geo/the_world.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TheWorldStandEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("adminspec", "textures/entity/stand/the_world.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TheWorldStandEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("adminspec", "animations/the_world.animation.json");
    }
}
