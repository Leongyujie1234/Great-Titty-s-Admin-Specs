package com.adminspec.spec;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * A single move belonging to a Spec. Moves are identified by an id (e.g. "reverse_flow_seal")
 * and are bound to a 1-4 index on the player.
 */
public abstract class SpecMove {

    private final String id;
    private final Component displayName;
    private final Component description;

    protected SpecMove(String id, Component displayName, Component description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return displayName;
    }

    public Component description() {
        return description;
    }

    /**
     * Called when the player activates this move (keybind press).
     * Implementations should branch on ctx.pressed() for toggle-style moves.
     */
    public abstract void activate(MoveContext ctx);

    /**
     * Called every server tick for the player who has this move bound.
     * Use this for sustained effects, cooldowns, regen, etc.
     */
    public void tick(Player player) {}

    /**
     * Called when the player loses this spec (swapped or cleared).
     */
    public void onRemoved(Player player) {}
}
