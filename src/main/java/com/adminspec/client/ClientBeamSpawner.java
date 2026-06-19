/*
 * Decompiled with CFR 0.152.
 */
package com.adminspec.client;

import com.adminspec.client.ClientBeamManager;

public final class ClientBeamSpawner {
    private ClientBeamSpawner() {
    }

    public static void spawnBeam(double startX, double startY, double startZ, double endX, double endY, double endZ) {
        ClientBeamManager.addBeam(startX, startY, startZ, endX, endY, endZ);
    }
}

