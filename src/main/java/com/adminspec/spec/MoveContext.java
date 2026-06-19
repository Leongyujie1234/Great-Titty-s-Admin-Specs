/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package com.adminspec.spec;

import net.minecraft.world.entity.player.Player;

public final class MoveContext {
    private final Player player;
    private final boolean pressed;

    public MoveContext(Player player, boolean pressed) {
        this.player = player;
        this.pressed = pressed;
    }

    public Player player() {
        return this.player;
    }

    public boolean pressed() {
        return this.pressed;
    }
}

