package com.adminspec.moves.guyue;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.entity.GiantHandEntity;
import com.adminspec.entity.ModEntities;
import com.adminspec.spec.MoveContext;
import com.adminspec.spec.SpecMove;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Move 2: Giant Hand
 *
 * Summons a GiantHandEntity high above the player's target location.
 * The hand falls VERY slowly (hand-controlled ~5 blocks/sec, but visually feels heavy).
 * On landing, deals 12 hearts in a 5-block AoE with knockback.
 *
 * Cooldown: 12 seconds.
 */
public class GiantHandMove extends SpecMove {

    public static final String ID = "giant_hand";
    private static final int COOLDOWN_TICKS = 12 * 20;

    public GiantHandMove() {
        super(ID,
                Component.literal("Giant Hand"),
                Component.literal("Summons a giant hand that slowly falls and smashes the ground. 12-heart AoE."));
    }

    @Override
    public void activate(MoveContext ctx) {
        Player player = ctx.player();
        if (player.level().isClientSide) return;
        if (!ctx.pressed()) return;

        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (data.getGiantHandCooldown() > 0) {
            player.sendSystemMessage(Component.literal("\u00a77[Giant Hand] Cooling down: "
                    + (data.getGiantHandCooldown() / 20) + "s"));
            return;
        }

        // Summon 18 blocks above the player's look target (or just above the player).
        Vec3 look = player.getLookAngle();
        Vec3 target = player.position().add(0, 1.0, 0).add(look.scale(8.0));
        Vec3 spawn = target.add(0, 18.0, 0);

        GiantHandEntity hand = new GiantHandEntity(ModEntities.GIANT_HAND.get(), player.level(), player, spawn);
        player.level().addFreshEntity(hand);

        data.setGiantHandCooldown(COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("\u00a7e[Giant Hand] \u00a7rSummoned. The hand descends..."));
    }

    @Override
    public void tick(Player player) {
        if (player.level().isClientSide) return;
        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (data.getGiantHandCooldown() > 0) {
            data.setGiantHandCooldown(data.getGiantHandCooldown() - 1);
        }
    }
}
