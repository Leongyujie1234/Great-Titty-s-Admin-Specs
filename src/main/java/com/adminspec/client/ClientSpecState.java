/*
 * Decompiled with CFR 0.152.
 */
package com.adminspec.client;

import com.adminspec.network.SpecStatePayload;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientSpecState {
    private static final ConcurrentHashMap<UUID, Snapshot> STATES = new ConcurrentHashMap();

    private ClientSpecState() {
    }

    public static void update(SpecStatePayload payload) {
        STATES.put(payload.playerId(), new Snapshot(payload.reverseFlowActive(), payload.reverseFlowCapacity(), payload.dragonFormActive()));
    }

    public static Snapshot get(PlayerLike player) {
        return STATES.get(player.uuid());
    }

    public static Snapshot get(UUID uuid) {
        return STATES.get(uuid);
    }

    public static final class Snapshot {
        public boolean reverseFlowActive;
        public float reverseFlowCapacity;
        public boolean dragonFormActive;
        public long lastUpdate;

        public Snapshot(boolean active, float cap, boolean dragon) {
            this.reverseFlowActive = active;
            this.reverseFlowCapacity = cap;
            this.dragonFormActive = dragon;
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    public static interface PlayerLike {
        public UUID uuid();
    }
}

