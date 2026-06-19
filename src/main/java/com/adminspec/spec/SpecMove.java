/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 */
package com.adminspec.spec;

import com.adminspec.spec.MoveContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

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
        return this.id;
    }

    public Component displayName() {
        return this.displayName;
    }

    public Component description() {
        return this.description;
    }

    public abstract void activate(MoveContext var1);

    public void tick(Player player) {
    }

    public void onRemoved(Player player) {
    }
}

