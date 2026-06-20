/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  org.joml.Vector3f
 */
package com.adminspec.moves.guyue;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.network.SpecStatePayload;
import com.adminspec.spec.MoveContext;
import com.adminspec.spec.SpecMove;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

public class ReverseFlowProtectionSealMove
extends SpecMove {
    public static final String ID = "reverse_flow_protection_seal";
    private static final float REFILL_PER_TICK = 0.001f;

    public ReverseFlowProtectionSealMove() {
        super(ID, (Component)Component.literal((String)"Reverse Flow Protection Seal"), (Component)Component.literal((String)"Toggle. Surrounds you with a flowing water robe. Immune to damage; attacks are reflected back at the attacker (you still take knockback). The Reverse Flow River depletes on reflect and refills over time."));
    }

    @Override
    public void activate(MoveContext ctx) {
        boolean nowOn;
        Player player = ctx.player();
        if (player.level().isClientSide) {
            return;
        }
        if (!ctx.pressed()) {
            return;
        }
        PlayerSpecData data = PlayerSpecCapability.get(player);
        boolean bl = nowOn = !data.isReverseFlowActive();
        if (nowOn) {
            if (data.getReverseFlowCapacity() <= 0.0f) {
                player.sendSystemMessage((Component)Component.literal((String)"\u00a7b[Reverse Flow River] \u00a7rThe river is dry. Wait for it to refill."));
                return;
            }
            data.setReverseFlowActive(true);
            player.sendSystemMessage((Component)Component.literal((String)"\u00a7b[Reverse Flow Protection Seal] \u00a7aENGAGED."));
        } else {
            data.setReverseFlowActive(false);
            player.sendSystemMessage((Component)Component.literal((String)"\u00a7b[Reverse Flow Protection Seal] \u00a7cDISENGAGED."));
        }
        SpecStatePayload.broadcast(player);
    }

    @Override
    public void tick(Player player) {
        if (player.level().isClientSide) {
            return;
        }
        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (data.isReverseFlowActive() && player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            ServerLevel sl = (ServerLevel)player.level();
            ReverseFlowProtectionSealMove.spawnRobeParticles(sl, sp);
        }
        if (!data.isReverseFlowActive() && data.getReverseFlowCapacity() < 1.0f) {
            data.setReverseFlowCapacity(data.getReverseFlowCapacity() + 0.001f);
        }
    }

    private static void spawnRobeParticles(ServerLevel sl, ServerPlayer player) {
        double angle;
        int i;
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();
        // Dense blue dust ring around the player — visible to self and others
        for (i = 0; i < 16; ++i) {
            angle = Math.random() * Math.PI * 2.0;
            double y = py + 0.3 + Math.random() * 1.5;
            double radius = 0.7 + (Math.random() - 0.5) * 0.2;
            double dx = px + Math.cos(angle) * radius;
            double dz = pz + Math.sin(angle) * radius;
            sl.sendParticles(player, (ParticleOptions)new DustParticleOptions(new Vector3f(0.3f, 0.7f, 1.0f), 3.0f), true, dx, y, dz, 1, 0.02, -0.03, 0.02, 0.0);
            sl.sendParticles((ParticleOptions)new DustParticleOptions(new Vector3f(0.3f, 0.7f, 1.0f), 3.0f), dx, y, dz, 1, 0.02, -0.03, 0.02, 0.0);
        }
        // Bright end rod sparkles for visibility
        for (i = 0; i < 4; ++i) {
            angle = Math.random() * Math.PI * 2.0;
            double y = py + 0.5 + Math.random() * 1.2;
            double radius = 0.6 + Math.random() * 0.3;
            double dx = px + Math.cos(angle) * radius;
            double dz = pz + Math.sin(angle) * radius;
            sl.sendParticles(ParticleTypes.END_ROD, dx, y, dz, 1, 0.05, 0.05, 0.05, 0.01);
        }
        for (i = 0; i < 3; ++i) {
            angle = Math.random() * Math.PI * 2.0;
            double dx = px + Math.cos(angle) * 0.45;
            double dz = pz + Math.sin(angle) * 0.45;
            sl.sendParticles(player, (ParticleOptions)ParticleTypes.FALLING_WATER, true, dx, py + 0.3, dz, 1, 0.0, -0.05, 0.0, 0.0);
            sl.sendParticles((ParticleOptions)ParticleTypes.FALLING_WATER, dx, py + 0.3, dz, 1, 0.0, -0.05, 0.0, 0.0);
        }
        if (Math.random() < 0.4) {
            double angle2 = Math.random() * Math.PI * 2.0;
            double splashRadius = 0.65 + Math.random() * 0.3;
            double dx = px + Math.cos(angle2) * splashRadius;
            double dz = pz + Math.sin(angle2) * splashRadius;
            sl.sendParticles(player, (ParticleOptions)ParticleTypes.SPLASH, true, dx, py + 0.1, dz, 2, 0.0, 0.1, 0.0, 0.1);
            sl.sendParticles((ParticleOptions)ParticleTypes.SPLASH, dx, py + 0.1, dz, 2, 0.0, 0.1, 0.0, 0.1);
        }
    }
}

