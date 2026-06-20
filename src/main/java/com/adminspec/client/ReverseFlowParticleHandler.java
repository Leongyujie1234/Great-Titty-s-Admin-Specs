package com.adminspec.client;

import com.adminspec.client.ClientSpecState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Random;

@EventBusSubscriber(modid="adminspec", bus=EventBusSubscriber.Bus.GAME, value={Dist.CLIENT})
public final class ReverseFlowParticleHandler {
    private static final Random RANDOM = new Random();

    private ReverseFlowParticleHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        for (Player player : level.players()) {
            ClientSpecState.Snapshot snap = ClientSpecState.get(player.getUUID());
            if (snap == null || !snap.reverseFlowActive) continue;
            spawnWaterRobe(level, player);
        }
    }

    private static void spawnWaterRobe(ClientLevel level, Player player) {
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();
        int ticks = player.tickCount;

        // Helix 1: Dynamic rotating water spirals rising up
        for (int i = 0; i < 3; i++) {
            double angle = (ticks * 0.2) + (i * Math.PI * 2.0 / 3.0);
            double yOffset = ((ticks + i * 8) % 24) / 24.0 * 2.0; // 0 to 2.0 blocks high
            double radius = 0.6 + Math.sin(ticks * 0.05 + i) * 0.1; // Pulsing radius
            double dx = px + Math.cos(angle) * radius;
            double dz = pz + Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.SPLASH, dx, py + yOffset, dz, 0.0, 0.05, 0.0);
            level.addParticle(ParticleTypes.FALLING_WATER, dx, py + yOffset, dz, 0.0, -0.01, 0.0);
        }

        // Helix 2: Opposite rotating water spirals descending
        for (int i = 0; i < 2; i++) {
            double angle = (-ticks * 0.15) + (i * Math.PI);
            double yOffset = 2.0 - (((ticks + i * 12) % 24) / 24.0 * 2.0); // 2.0 to 0 block high
            double radius = 0.55;
            double dx = px + Math.cos(angle) * radius;
            double dz = pz + Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.DRIPPING_WATER, dx, py + yOffset, dz, 0.0, 0.0, 0.0);
            if (RANDOM.nextFloat() < 0.3f) {
                level.addParticle(ParticleTypes.INSTANT_EFFECT, dx, py + yOffset, dz, 0.1, 0.5, 0.9); // Blue swirl
            }
        }

        // Splash ring on ground
        if (ticks % 2 == 0) {
            double groundAngle = RANDOM.nextDouble() * Math.PI * 2.0;
            double groundRadius = 0.7 + RANDOM.nextDouble() * 0.2;
            double dx = px + Math.cos(groundAngle) * groundRadius;
            double dz = pz + Math.sin(groundAngle) * groundRadius;
            level.addParticle(ParticleTypes.SPLASH, dx, py + 0.05, dz, 0.0, 0.02, 0.0);
        }

        // Mist/Drip from head level
        if (RANDOM.nextFloat() < 0.2f) {
            double headAngle = RANDOM.nextDouble() * Math.PI * 2.0;
            double headRadius = RANDOM.nextDouble() * 0.5;
            double dx = px + Math.cos(headAngle) * headRadius;
            double dz = pz + Math.sin(headAngle) * headRadius;
            level.addParticle(ParticleTypes.DRIPPING_WATER, dx, py + 1.8, dz, 0.0, 0.0, 0.0);
        }
    }
}
