/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 */
package com.adminspec.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;

public final class MoveKeybinds {
    private static final Map<String, KeyMapping> BINDINGS = new LinkedHashMap<String, KeyMapping>();
    private static final Map<String, KeyMapping> BINDINGS_VIEW = Collections.unmodifiableMap(BINDINGS);

    private MoveKeybinds() {
    }

    public static void register(String moveId, KeyMapping mapping) {
        if (moveId == null || moveId.isEmpty()) {
            throw new IllegalArgumentException("moveId must not be null or empty");
        }
        if (mapping == null) {
            throw new IllegalArgumentException("mapping must not be null");
        }
        if (BINDINGS.containsKey(moveId)) {
            return;
        }
        BINDINGS.put(moveId, mapping);
    }

    public static KeyMapping get(String moveId) {
        return BINDINGS.get(moveId);
    }

    public static Map<String, KeyMapping> all() {
        return BINDINGS_VIEW;
    }

    public static int size() {
        return BINDINGS.size();
    }
}

