/*
 * Decompiled with CFR 0.152.
 */
package com.adminspec.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class ClientBeamManager {
    private static final List<Beam> BEAMS = new ArrayList<Beam>();
    private static final List<Beam> BEAMS_VIEW = Collections.unmodifiableList(BEAMS);
    public static final int DEFAULT_LIFETIME = 15;

    private ClientBeamManager() {
    }

    public static void addBeam(double sx, double sy, double sz, double ex, double ey, double ez) {
        BEAMS.add(new Beam(sx, sy, sz, ex, ey, ez, 15));
    }

    public static List<Beam> getBeams() {
        return BEAMS_VIEW;
    }

    public static void tick() {
        Iterator<Beam> it = BEAMS.iterator();
        while (it.hasNext()) {
            Beam b = it.next();
            ++b.tickCounter;
            if (b.tickCounter < b.maxLifetime) continue;
            it.remove();
        }
    }

    public static final class Beam {
        public final double startX;
        public final double startY;
        public final double startZ;
        public final double endX;
        public final double endY;
        public final double endZ;
        public int tickCounter;
        public final int maxLifetime;

        Beam(double sx, double sy, double sz, double ex, double ey, double ez, int lifetime) {
            this.startX = sx;
            this.startY = sy;
            this.startZ = sz;
            this.endX = ex;
            this.endY = ey;
            this.endZ = ez;
            this.tickCounter = 0;
            this.maxLifetime = lifetime;
        }
    }
}

