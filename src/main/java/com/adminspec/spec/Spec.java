package com.adminspec.spec;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A "Spec" is a character kit. It holds an ordered list of moves (index 0..N-1).
 * When a player has a spec active, the moves at indices 0..N-1 are bound to keys 1..N.
 */
public class Spec {

    private final String id;
    private final Component displayName;
    private final Component description;
    private final List<SpecMove> moves;

    public Spec(String id, Component displayName, Component description, List<SpecMove> moves) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.moves = Collections.unmodifiableList(new ArrayList<>(moves));
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

    public List<SpecMove> moves() {
        return moves;
    }

    /** Called every server tick for every player who has this spec active. */
    public void tick(Player player) {
        for (SpecMove m : moves) {
            m.tick(player);
        }
    }

    /** Called when this spec is removed from a player (swap/clear). */
    public void onRemoved(Player player) {
        for (SpecMove m : moves) {
            m.onRemoved(player);
        }
    }
}
