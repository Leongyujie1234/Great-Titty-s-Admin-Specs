package com.adminspec.moves.guyue;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.entity.ModEntities;
import com.adminspec.entity.SwordLightEntity;
import com.adminspec.spec.MoveContext;
import com.adminspec.spec.SpecMove;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Move 4: Five Finger Fist Heart Sword
 *
 * On activation:
 *   - Self-stun the player (Slowness + Mining Fatigue + Weakness at high amp for the duration).
 *   - Announce "First finger!" in chat.
 *   - Wait 1 second, then fire a Sword Light beam that travels fast (~8 blocks/tick, near-instant)
 *     in the player's look direction, dealing 6 hearts.
 *   - Repeat for second and third finger.
 *   - After the third, enter a 10-second cooldown.
 *
 * The player can still rotate while stunned, so each finger can independently pick its direction
 * when it fires (not when the move starts).
 */
public class FiveFingerFistHeartSwordMove extends SpecMove {

    public static final String ID = "five_finger_fist_heart_sword";
    private static final int COOLDOWN_TICKS = 10 * 20;
    private static final int TICKS_BETWEEN_FINGERS = 20; // 1s
    private static final float DAMAGE = 12.0f;           // 6 hearts

    public FiveFingerFistHeartSwordMove() {
        super(ID,
                Component.literal("Five Finger Fist Heart Sword"),
                Component.literal("Self-stun and announce three successive fingers, each firing an instant sword light dealing 6 hearts."));
    }

    @Override
    public void activate(MoveContext ctx) {
        Player player = ctx.player();
        if (player.level().isClientSide) return;
        if (!ctx.pressed()) return;

        PlayerSpecData data = PlayerSpecCapability.get(player);

        if (data.getHeartSwordState() > 0) {
            player.sendSystemMessage(Component.literal("\u00a77[Five Finger Fist Heart Sword] \u00a77Already channeling."));
            return;
        }
        if (data.getHeartSwordCooldown() > 0) {
            player.sendSystemMessage(Component.literal("\u00a77[Five Finger Fist Heart Sword] \u00a77Cooling down: "
                    + (data.getHeartSwordCooldown() / 20) + "s"));
            return;
        }

        // Begin channeling.
        data.setHeartSwordState(1);
        data.setHeartSwordTimer(TICKS_BETWEEN_FINGERS);
        beginStun(player);
        announceFinger(player, 1);
    }

    @Override
    public void tick(Player player) {
        if (player.level().isClientSide) return;
        PlayerSpecData data = PlayerSpecCapability.get(player);

        if (data.getHeartSwordState() > 0) {
            // Maintain stun while channeling.
            maintainStun(player);
        } else if (data.getHeartSwordCooldown() > 0) {
            data.setHeartSwordCooldown(data.getHeartSwordCooldown() - 1);
        }
    }

    @Override
    public void onRemoved(Player player) {
        if (player.level().isClientSide) return;
        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (data.getHeartSwordState() > 0) {
            data.setHeartSwordState(0);
            data.setHeartSwordTimer(0);
            endStun(player);
        }
    }

    // ----------------- Helpers -----------------

    private static void beginStun(Player player) {
        // Heavy slowness + mining fatigue + weakness = effectively a self-stun (can't move or attack).
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 5, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 5 * 20, 5, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 5 * 20, 5, false, false, false));
    }

    private static void maintainStun(Player player) {
        // Re-up slowness if it's about to expire.
        MobEffectInstance slow = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (slow == null || slow.getDuration() < 10) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 5, false, false, false));
        }
    }

    private static void endStun(Player player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);
    }

    private static void announceFinger(Player player, int finger) {
        String name = switch (finger) {
            case 1 -> "First finger!";
            case 2 -> "Second finger!";
            case 3 -> "Third finger!";
            default -> "Finger!";
        };
        // Announce to all players on the server (matches the "saying in chat" requirement).
        if (player.getServer() != null) {
            player.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("\u00a7d" + player.getDisplayName().getString() + ": \u00a7f" + name), false);
        } else {
            player.sendSystemMessage(Component.literal("\u00a7d" + player.getDisplayName().getString() + ": \u00a7f" + name));
        }
    }

    /**
     * Called by PlayerSpecData.serverTick when the timer for the current finger expires.
     * Fires the beam, advances to the next finger, or completes the move.
     */
    public static void fireFinger(Player player, int finger) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 start = eye.add(look.scale(1.0)); // 1 block in front of eyes so we don't self-hit

        SwordLightEntity beam = new SwordLightEntity(ModEntities.SWORD_LIGHT.get(), player.level(), player, start, look);
        player.level().addFreshEntity(beam);

        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    start.x, start.y, start.z, 12, 0.1, 0.1, 0.1, 0.05);
        }
    }
}
