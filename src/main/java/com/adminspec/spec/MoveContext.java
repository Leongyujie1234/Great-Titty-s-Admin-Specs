package com.adminspec.spec;

import net.minecraft.world.entity.player.Player;

/**
 * Context passed to a SpecMove when the player triggers it (via keybind or command).
 */
public final class MoveContext {
    private final Player player;
    private final boolean pressed; // true = key just pressed, false = key just released (for toggles)

    public MoveContext(Player player, boolean pressed) {
        this.player = player;
        this.pressed = pressed;
    }

    public Player player() {
        return player;
    }

    public boolean pressed() {
        return pressed;
    }
}
