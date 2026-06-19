/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.monster.Zombie
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.adminspec.moves.guyue;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.entity.ModEntities;
import com.adminspec.entity.YamaChildEntity;
import com.adminspec.spec.MoveContext;
import com.adminspec.spec.SpecMove;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class YamaChildrenMove
extends SpecMove {
    public static final String ID = "yama_children";
    private static final int COOLDOWN_TICKS = 60;
    private static final int MAX_CHILDREN = 3;

    public YamaChildrenMove() {
        super(ID, (Component)Component.literal((String)"Yama Children"), (Component)Component.literal((String)"Summon a flying baby zombie that detonates near enemies. 2x TNT damage. Destruction rebuilds after 2 minutes. 3s cooldown."));
    }

    @Override
    public void activate(MoveContext ctx) {
        Player player = ctx.player();
        if (player.level().isClientSide) {
            return;
        }
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)player;
        if (!ctx.pressed()) {
            return;
        }
        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (data.getYamaChildrenCooldown() > 0) {
            sp.sendSystemMessage((Component)Component.literal((String)("\u00a78[Yama Children] \u00a77Cooling down: " + data.getYamaChildrenCooldown() / 20 + "s")));
            return;
        }
        ServerLevel sl = (ServerLevel)player.level();
        long alive = sl.getEntitiesOfClass(YamaChildEntity.class, player.getBoundingBox().inflate(64.0)).stream().filter(yc -> yc.getOwnerPlayer() == player).count();
        if (alive >= 3L) {
            sp.sendSystemMessage((Component)Component.literal((String)"\u00a78[Yama Children] \u00a7cMaximum Yama Children in play (3)."));
            return;
        }
        Vec3 spawnPos = player.position().add(player.getLookAngle().scale(2.0)).add(0.0, -0.5, 0.0);
        YamaChildEntity child = new YamaChildEntity((EntityType<? extends Zombie>)((EntityType)ModEntities.YAMA_CHILD.get()), (Level)sl);
        child.setPos(spawnPos);
        child.setOwner(player);
        sl.addFreshEntity((Entity)child);
        data.setYamaChildrenCooldown(60);
        sp.sendSystemMessage((Component)Component.literal((String)"\u00a78[Yama Children] \u00a7rA Yama Child crawls forth."));
    }

    @Override
    public void tick(Player player) {
        if (player.level().isClientSide) {
            return;
        }
        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (data.getYamaChildrenCooldown() > 0) {
            data.setYamaChildrenCooldown(data.getYamaChildrenCooldown() - 1);
        }
    }
}

