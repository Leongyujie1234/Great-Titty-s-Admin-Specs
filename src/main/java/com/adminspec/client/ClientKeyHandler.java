package com.adminspec.client;

import com.adminspec.network.ActivateMovePayload;
import com.adminspec.network.DragonFlightInputPayload;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid="adminspec", bus=EventBusSubscriber.Bus.GAME, value={Dist.CLIENT})
public final class ClientKeyHandler {
    private ClientKeyHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ClientSpecState.clientFlashTicks > 0) {
            ClientSpecState.clientFlashTicks--;
        }
        ClientBeamManager.tick();

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        // Smoothly increment dragonFormTicks locally on client every tick
        for (ClientSpecState.Snapshot snap : ClientSpecState.allSnapshots()) {
            if (snap.dragonFormActive && snap.dragonFormTicks < 60) {
                snap.dragonFormTicks++;
            }
        }

        // Dragon form: send flight input (jump/sneak/forward/strafe) to server every tick
        ClientSpecState.Snapshot localSnap = ClientSpecState.get(player.getUUID());
        if (localSnap != null && localSnap.dragonFormActive) {
            boolean jumping = mc.options.keyJump.isDown();
            boolean sneaking = mc.options.keyShift.isDown();
            // forward: W = +1, S = -1; strafe: A = -1, D = +1
            float forward = 0f;
            float strafe  = 0f;
            if (mc.options.keyUp.isDown())    forward += 1f;
            if (mc.options.keyDown.isDown())  forward -= 1f;
            if (mc.options.keyLeft.isDown())  strafe  -= 1f;
            if (mc.options.keyRight.isDown()) strafe  += 1f;

            PacketDistributor.sendToServer(
                new DragonFlightInputPayload(jumping, sneaking, forward, strafe),
                new CustomPacketPayload[0]
            );
        }

        // Process move keybinds
        for (Map.Entry<String, KeyMapping> entry : MoveKeybinds.all().entrySet()) {
            if (!entry.getValue().consumeClick()) continue;
            sendActivate(entry.getKey());
        }
    }

    /**
     * Intercept attack (LMB) and use (RMB) while in dragon form.
     * LMB → dragon breath via server packet (handled separately by DragonBreathHandler).
     * RMB → fully suppressed.
     */
    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ClientSpecState.Snapshot snap = ClientSpecState.get(player.getUUID());
        if (snap != null && snap.dragonFormActive) {
            if (event.isAttack() || event.isUseItem()) {
                // Cancel both attack and use – breath is triggered by DragonBreathHandler
                event.setCanceled(true);
                // Do NOT call setSwingHand(true) – that would trigger an attack
            }
        }
    }

    private static void sendActivate(String moveId) {
        PacketDistributor.sendToServer(
            new ActivateMovePayload(moveId),
            new CustomPacketPayload[0]
        );
    }
}
