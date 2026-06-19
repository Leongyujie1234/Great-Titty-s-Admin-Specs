package com.adminspec.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public final class YamaChildRenderer extends ZombieRenderer {
    private static final ResourceLocation BLACK_TEXTURE = ResourceLocation.fromNamespaceAndPath("adminspec", "textures/entity/yama_child.png");

    public YamaChildRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        return BLACK_TEXTURE;
    }
}
