/*
 * Decompiled with CFR 0.152.
 */
package com.adminspec.client;

import com.adminspec.network.SpecStatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientSpecState {
    private static final ConcurrentHashMap<UUID, Snapshot> STATES = new ConcurrentHashMap();
    public static int clientFlashTicks = 0;

    private ClientSpecState() {
    }

    public static void update(SpecStatePayload payload) {
        Snapshot prev = STATES.get(payload.playerId());
        STATES.put(payload.playerId(), new Snapshot(payload.reverseFlowActive(), payload.reverseFlowCapacity(), payload.dragonFormActive(), payload.dragonFormTicks()));
        if (payload.dragonFormActive() && (prev == null || !prev.dragonFormActive)) {
            playTransformVfx(payload.playerId());
        }
    }

    private static void playTransformVfx(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        net.minecraft.world.entity.player.Player p = mc.level.getPlayerByUUID(uuid);
        if (p == null) return;

        double dist = mc.player != null ? mc.player.distanceTo(p) : 999.0;
        if (dist < 48.0) {
            if (p == mc.player) {
                ClientSpecState.clientFlashTicks = 20;
            } else if (dist < 24.0) {
                ClientSpecState.clientFlashTicks = 10;
            }

            mc.level.playLocalSound(p.getX(), p.getY(), p.getZ(),
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                net.minecraft.sounds.SoundSource.PLAYERS, 2.0f, 0.75f, false);

            for (int i = 0; i < 60; i++) {
                double rx = p.getX() + (p.getRandom().nextDouble() - 0.5) * 4.0;
                double ry = p.getY() + p.getRandom().nextDouble() * 3.0;
                double rz = p.getZ() + (p.getRandom().nextDouble() - 0.5) * 4.0;
                mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, rx, ry, rz, 0.0, 0.15, 0.0);
                mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.FLASH, rx, ry, rz, 0.0, 0.0, 0.0);
                mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, rx, ry, rz,
                    (p.getRandom().nextDouble() - 0.5) * 0.1, 0.05, (p.getRandom().nextDouble() - 0.5) * 0.1);
            }
        }
    }

    public static Snapshot get(PlayerLike player) {
        return STATES.get(player.uuid());
    }

    public static Snapshot get(UUID uuid) {
        return STATES.get(uuid);
    }

    public static java.util.Collection<Snapshot> allSnapshots() {
        return STATES.values();
    }

    public static final class Snapshot {
        public boolean reverseFlowActive;
        public float reverseFlowCapacity;
        public boolean dragonFormActive;
        public int dragonFormTicks;
        public long lastUpdate;

        public Snapshot(boolean active, float cap, boolean dragon, int dragonTicks) {
            this.reverseFlowActive = active;
            this.reverseFlowCapacity = cap;
            this.dragonFormActive = dragon;
            this.dragonFormTicks = dragonTicks;
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    public static interface PlayerLike {
        public UUID uuid();
    }
}

