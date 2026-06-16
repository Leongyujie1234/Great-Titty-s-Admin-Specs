package com.adminspec.capability;

import com.adminspec.AdminSpecMod;
import com.adminspec.moves.guyue.ReverseFlowProtectionSealMove;
import com.adminspec.network.SpecStatePayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-side event handlers for spec system: per-tick player spec ticking, the
 * Reverse Flow Protection Seal damage hook, and periodic spec-state sync to clients
 * (for outline rendering).
 */
@EventBusSubscriber(modid = AdminSpecMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpecEvents {

    private static int syncTickCounter = 0;

    private SpecEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        var server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerSpecCapability.tickPlayer(player);
        }

        // Broadcast spec state to all clients every 5 ticks (0.25s).
        if (++syncTickCounter >= 5) {
            syncTickCounter = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PlayerSpecData data = PlayerSpecCapability.get(player);
                if (data.getSpecId() == null) continue;
                PacketDistributor.sendToAllPlayers(new SpecStatePayload(
                        player.getUUID(),
                        data.isReverseFlowActive(),
                        data.isYamaActive(),
                        data.getReverseFlowCapacity()));
            }
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        DamageSource source = event.getSource();
        float amount = event.getAmount();

        if (ReverseFlowProtectionSealMove.tryAbsorb(player, source, amount)) {
            event.setCanceled(true);
        }
    }
}

