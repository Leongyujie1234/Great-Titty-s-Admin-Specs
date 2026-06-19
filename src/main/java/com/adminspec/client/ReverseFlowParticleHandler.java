/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  org.joml.Vector3f
 */
package com.adminspec.client;

import com.adminspec.client.ClientSpecState;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid="adminspec", bus=EventBusSubscriber.Bus.GAME, value={Dist.CLIENT})
public final class ReverseFlowParticleHandler {
    private static final DustParticleOptions WATER_DUST = new DustParticleOptions(new Vector3f(0.2f, 0.5f, 1.0f), 1.5f);
    private static final double ROBE_RADIUS = 0.45;
    private static final double ROBE_TOP_Y = 1.6;
    private static final double ROBE_BOTTOM_Y = 0.3;
    private static final int ROBE_PARTICLES_PER_TICK = 12;
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
            ReverseFlowParticleHandler.spawnWaterRobe(level, player);
        }
    }

    private static void spawnWaterRobe(ClientLevel level, Player player) {
        double angle;
        int i;
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();
        for (i = 0; i < 12; ++i) {
            angle = RANDOM.nextDouble() * Math.PI * 2.0;
            double y = py + 0.3 + RANDOM.nextDouble() * 1.3;
            double radius = 0.45 + (RANDOM.nextDouble() - 0.5) * 0.1;
            double dx = px + Math.cos(angle) * radius;
            double dz = pz + Math.sin(angle) * radius;
            double velX = (RANDOM.nextDouble() - 0.5) * 0.02;
            double velY = -0.03 - RANDOM.nextDouble() * 0.02;
            double velZ = (RANDOM.nextDouble() - 0.5) * 0.02;
            level.addParticle((ParticleOptions)WATER_DUST, dx, y, dz, velX, velY, velZ);
        }
        for (i = 0; i < 4; ++i) {
            angle = RANDOM.nextDouble() * Math.PI * 2.0;
            double dx = px + Math.cos(angle) * 0.45;
            double dz = pz + Math.sin(angle) * 0.45;
            level.addParticle((ParticleOptions)ParticleTypes.FALLING_WATER, dx, py + 0.3, dz, 0.0, -0.05, 0.0);
        }
        if (RANDOM.nextFloat() < 0.3f) {
            double angle2 = RANDOM.nextDouble() * Math.PI * 2.0;
            double dx = px + Math.cos(angle2) * 0.55;
            double dz = pz + Math.sin(angle2) * 0.55;
            level.addParticle((ParticleOptions)ParticleTypes.DRIPPING_WATER, dx, py + 1.6, dz, 0.0, 0.0, 0.0);
        }
        if (RANDOM.nextFloat() < 0.4f) {
            double angle3 = RANDOM.nextDouble() * Math.PI * 2.0;
            double splashRadius = 0.65 + RANDOM.nextDouble() * 0.3;
            double dx = px + Math.cos(angle3) * splashRadius;
            double dz = pz + Math.sin(angle3) * splashRadius;
            level.addParticle((ParticleOptions)ParticleTypes.SPLASH, dx, py + 0.1, dz, 0.0, 0.1, 0.0);
        }
    }
}

