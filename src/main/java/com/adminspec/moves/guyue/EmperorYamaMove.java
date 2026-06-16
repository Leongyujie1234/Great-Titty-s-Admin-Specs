package com.adminspec.moves.guyue;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.entity.ModEntities;
import com.adminspec.entity.YamaChildEntity;
import com.adminspec.spec.MoveContext;
import com.adminspec.spec.SpecMove;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Move 3: Emperor Yama (toggle)
 *
 *  - Toggle ON:  player gains a black outline and creative-style flight.
 *  - Toggle OFF: flight disabled, outline removed.
 *  - While ON, every subsequent press of the move-3 key SUMMONS a Yama Child at the player's
 *    position (instead of toggling). To toggle the form, sneak + press move 3.
 *
 * Cooldown between summons: 3 seconds. Max 3 Yama Children alive at once per player.
 */
public class EmperorYamaMove extends SpecMove {

    public static final String ID = "emperor_yama";
    private static final int SUMMON_COOLDOWN = 3 * 20;
    private static final int MAX_CHILDREN = 3;

    public EmperorYamaMove() {
        super(ID,
                Component.literal("Emperor Yama"),
                Component.literal("Toggle: gain flight + black outline. While active, press to summon Yama Children. Sneak+press to toggle off."));
    }

    @Override
    public void activate(MoveContext ctx) {
        Player player = ctx.player();
        if (player.level().isClientSide) return;
        if (!ctx.pressed()) return;

        PlayerSpecData data = PlayerSpecCapability.get(player);

        if (player.isShiftKeyDown()) {
            // Toggle the form off (or back on).
            boolean nowOn = !data.isYamaActive();
            data.setYamaActive(nowOn);
            applyFlight(player, nowOn);
            player.sendSystemMessage(Component.literal("\u00a78[Emperor Yama] \u00a7r"
                    + (nowOn ? "\u00a7aForm assumed. Flight granted." : "\u00a7cForm released.")));
            return;
        }

        // If form isn't active, treat this as the toggle-on press.
        if (!data.isYamaActive()) {
            data.setYamaActive(true);
            applyFlight(player, true);
            player.sendSystemMessage(Component.literal("\u00a78[Emperor Yama] \u00a7aForm assumed. Flight granted. Press again (sneak+key) to release."));
            return;
        }

        // Otherwise: summon a Yama Child.
        if (data.getYamaSummonCooldown() > 0) {
            player.sendSystemMessage(Component.literal("\u00a77[Emperor Yama] \u00a77Summon cooling down: "
                    + (data.getYamaSummonCooldown() / 20) + "s"));
            return;
        }

        long alive = player.level().getEntitiesOfClass(YamaChildEntity.class,
                        player.getBoundingBox().inflate(64))
                .stream()
                .filter(yc -> yc.getOwnerPlayer() == player)
                .count();
        if (alive >= MAX_CHILDREN) {
            player.sendSystemMessage(Component.literal("\u00a77[Emperor Yama] \u00a7cMaximum Yama Children in play ("
                    + MAX_CHILDREN + ")."));
            return;
        }

        Vec3 spawnPos = player.position().add(player.getLookAngle().scale(2.0)).add(0, -0.5, 0);
        YamaChildEntity child = new YamaChildEntity(ModEntities.YAMA_CHILD.get(), player.level(), player);
        child.setPos(spawnPos);
        player.level().addFreshEntity(child);

        data.setYamaSummonCooldown(SUMMON_COOLDOWN);
        player.sendSystemMessage(Component.literal("\u00a78[Emperor Yama] \u00a7rA Yama Child crawls forth."));
    }

    @Override
    public void tick(Player player) {
        if (player.level().isClientSide) return;
        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (data.getYamaSummonCooldown() > 0) {
            data.setYamaSummonCooldown(data.getYamaSummonCooldown() - 1);
        }
        // Ensure flight stays on while form is active.
        if (data.isYamaActive()) {
            applyFlight(player, true);
        }
    }

    @Override
    public void onRemoved(Player player) {
        if (player.level().isClientSide) return;
        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (data.isYamaActive()) {
            data.setYamaActive(false);
            applyFlight(player, false);
        }
    }

    private static void applyFlight(Player player, boolean fly) {
        // Only grant flight on the server; creative-style flight uses the abilities flag.
        if (!player.getAbilities().mayfly || !fly) {
            // When disabling, also disable currently-flying state.
            if (!fly) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            } else {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
            }
            player.onUpdateAbilities();
        } else if (fly) {
            // Ensure flying is actually engaged.
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }
    }
}
