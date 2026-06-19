/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 */
package com.adminspec.spec;

import com.adminspec.spec.SpecMove;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class Spec {
    private final String id;
    private final Component displayName;
    private final Component description;
    private final List<SpecMove> moves;

    public Spec(String id, Component displayName, Component description, List<SpecMove> moves) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.moves = Collections.unmodifiableList(new ArrayList<SpecMove>(moves));
    }

    public String id() {
        return this.id;
    }

    public Component displayName() {
        return this.displayName;
    }

    public Component description() {
        return this.description;
    }

    public List<SpecMove> moves() {
        return this.moves;
    }

    public void tick(Player player) {
        for (SpecMove m : this.moves) {
            m.tick(player);
        }
    }

    public void onRemoved(Player player) {
        for (SpecMove m : this.moves) {
            m.onRemoved(player);
        }
    }
}

