package com.adminspec.client;

import com.adminspec.network.DragonBreathPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends the dragon breath packet to the server while M1 is held in dragon form.
 * Rate-limited to once per 60 ticks (3 seconds) by server-side cooldown.
 * The actual VFX is delivered via DragonBreathVfxPayload from the server.
 */
@EventBusSubscriber(modid="adminspec", bus=EventBusSubscriber.Bus.GAME, value={Dist.CLIENT})
public final class DragonBreathHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("adminspec-breath");

    // Local client cooldown to avoid spamming the server
    private static int localCooldown = 0;

    private DragonBreathHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (localCooldown > 0) localCooldown--;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ClientSpecState.Snapshot snap = ClientSpecState.get(mc.player.getUUID());
        if (snap == null || !snap.dragonFormActive) return;

        // Fire breath when M1 is held and cooldown expired
        if (mc.options.keyAttack.isDown() && localCooldown == 0) {
            LOGGER.info("[AdminSpec] Dragon breath triggered, sending payload");
            PacketDistributor.sendToServer(
                new DragonBreathPayload(),
                new CustomPacketPayload[0]
            );
            localCooldown = 60; // match server cooldown
        }
    }
}
