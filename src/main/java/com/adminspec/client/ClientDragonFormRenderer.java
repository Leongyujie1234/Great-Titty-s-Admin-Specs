package com.adminspec.client;

import com.adminspec.client.ClientSpecState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = "adminspec", bus = EventBusSubscriber.Bus.GAME, value = {Dist.CLIENT})
public final class ClientDragonFormRenderer {

    private ClientDragonFormRenderer() {}

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        UUID uuid = player.getUUID();
        ClientSpecState.Snapshot snapshot = ClientSpecState.get(uuid);
        if (snapshot != null && snapshot.dragonFormActive) {
            event.setCanceled(true);

            PoseStack pose = event.getPoseStack();
            pose.pushPose();

            // Center the statue on the player's bounding box
            pose.translate(0.0, 0.75, 0.0);

            // Match look orientation
            float yaw = player.getViewYRot(event.getPartialTick());
            float pitch = player.getViewXRot(event.getPartialTick());
            pose.mulPose(Axis.YP.rotationDegrees(-yaw));
            pose.mulPose(Axis.XP.rotationDegrees(pitch));

            // Scale to look like a block statue of a dragon
            pose.scale(3.0f, 3.0f, 3.0f);

            ItemStack itemStack = new ItemStack(Blocks.DRAGON_HEAD);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                itemStack,
                ItemDisplayContext.GROUND,
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY,
                pose,
                event.getMultiBufferSource(),
                player.level(),
                0
            );

            pose.popPose();
        }
    }
}
