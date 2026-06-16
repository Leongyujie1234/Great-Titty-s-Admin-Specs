package com.adminspec.client;

import com.adminspec.network.SpecStatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache of spec state for all players (used by the outline renderer).
 * Updated by SpecStatePayload packets from the server.
 */
public final class ClientSpecState {

    public static final class Snapshot {
        public boolean reverseFlowActive;
        public boolean yamaActive;
        public float reverseFlowCapacity;
        public long lastUpdate;

        public Snapshot(boolean rf, boolean yama, float cap) {
            this.reverseFlowActive = rf;
            this.yamaActive = yama;
            this.reverseFlowCapacity = cap;
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    private static final ConcurrentHashMap<UUID, Snapshot> STATES = new ConcurrentHashMap<>();

    private ClientSpecState() {}

    public static void update(SpecStatePayload payload) {
        STATES.put(payload.playerId(),
                new Snapshot(payload.reverseFlowActive(), payload.yamaActive(), payload.reverseFlowCapacity()));
    }

    public static Snapshot get(Player player) {
        return STATES.get(player.getUUID());
    }

    public static void clear(UUID id) {
        STATES.remove(id);
    }
}
