package com.adminspec.client;

import com.adminspec.AdminSpecMod;
import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.network.ActivateMovePayload;
import com.adminspec.network.SpecStatePayload;
import com.adminspec.spec.Spec;
import com.adminspec.spec.SpecRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client-side keybind handler — translates move key presses into packets sent to the server.
 */
@EventBusSubscriber(modid = AdminSpecMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ClientKeyHandler {

    private ClientKeyHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        // Poll each move key. We send a "pressed" payload when the key transitions from up to down.
        if (ClientSetup.KEY_MOVE_1.consumeClick()) {
            sendActivate(0);
        }
        if (ClientSetup.KEY_MOVE_2.consumeClick()) {
            sendActivate(1);
        }
        if (ClientSetup.KEY_MOVE_3.consumeClick()) {
            sendActivate(2);
        }
        if (ClientSetup.KEY_MOVE_4.consumeClick()) {
            sendActivate(3);
        }
    }

    private static void sendActivate(int moveIndex) {
        // 0-based here; server-side converts to 1-based.
        PacketDistributor.sendToServer(new ActivateMovePayload(moveIndex));
    }
}
