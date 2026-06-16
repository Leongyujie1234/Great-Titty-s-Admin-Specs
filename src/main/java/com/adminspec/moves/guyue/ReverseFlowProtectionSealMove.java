package com.adminspec.moves.guyue;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.spec.MoveContext;
import com.adminspec.spec.SpecMove;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/**
 * Move 1: Reverse Flow Protection Seal (toggle)
 *
 *  - Toggling on gives the player a blue outline (rendered client-side via outline pass).
 *  - While toggled ON, the player cannot be damaged, and any attack is reversed:
 *      - melee attack -> attacker takes the same damage
 *      - projectile hit -> projectile is reflected straight back at the attacker (or away if no attacker)
 *  - Reversing an attack drains the Reverse Flow River (capacity 0..1).
 *      - Melee reversal costs ~5% capacity; projectile reversal ~10%.
 *      - When capacity hits 0, the seal auto-disables.
 *  - When toggled OFF, the river refills at ~2%/sec.
 */
public class ReverseFlowProtectionSealMove extends SpecMove {

    public static final String ID = "reverse_flow_protection_seal";

    private static final float MELEE_COST = 0.05f;
    private static final float PROJECTILE_COST = 0.10f;
    private static final float REFILL_PER_TICK = 0.02f / 20f;

    public ReverseFlowProtectionSealMove() {
        super(ID,
                Component.literal("Reverse Flow Protection Seal"),
                Component.literal("Toggle. Invulnerable + reflects melee / returns projectiles. Drains the Reverse Flow River."));
    }

    @Override
    public void activate(MoveContext ctx) {
        Player player = ctx.player();
        if (player.level().isClientSide) return;
        if (!ctx.pressed()) return;

        PlayerSpecData data = PlayerSpecCapability.get(player);
        boolean nowOn = !data.isReverseFlowActive();

        if (nowOn && data.getReverseFlowCapacity() <= 0f) {
            player.sendSystemMessage(Component.literal("\u00a7b[Reverse Flow River] \u00a7rThe river is dry. Wait for it to refill."));
            return;
        }

        data.setReverseFlowActive(nowOn);
        player.sendSystemMessage(Component.literal("\u00a7b[Reverse Flow Protection Seal] \u00a7r"
                + (nowOn ? "\u00a7aENGAGED" : "\u00a7cDISENGAGED")));
    }

    @Override
    public void tick(Player player) {
        if (player.level().isClientSide) return;
        PlayerSpecData data = PlayerSpecCapability.get(player);

        if (!data.isReverseFlowActive()) {
            if (data.getReverseFlowCapacity() < 1f) {
                data.setReverseFlowCapacity(data.getReverseFlowCapacity() + REFILL_PER_TICK);
            }
            return;
        }

        // Slight passive drain while active to discourage permanent uptime.
        data.setReverseFlowCapacity(data.getReverseFlowCapacity() - 0.001f / 20f);

        if (data.getReverseFlowCapacity() <= 0f) {
            data.setReverseFlowCapacity(0f);
            data.setReverseFlowActive(false);
            player.sendSystemMessage(Component.literal("\u00a7b[Reverse Flow Protection Seal] \u00a7cThe river has run dry. Seal disengaged."));
        }
    }

    @Override
    public void onRemoved(Player player) {
        if (player.level().isClientSide) return;
        PlayerSpecData data = PlayerSpecCapability.get(player);
        data.setReverseFlowActive(false);
    }

    /**
     * Called from a damage event handler. Returns true if the seal absorbed & reflected the damage.
     * If true, the caller must cancel the event (no damage applied to victim).
     */
    public static boolean tryAbsorb(Player victim, DamageSource source, float amount) {
        PlayerSpecData data = PlayerSpecCapability.get(victim);
        if (!data.isReverseFlowActive()) return false;
        if (data.getReverseFlowCapacity() <= 0f) return false;

        boolean isProjectile = source.getDirectEntity() instanceof Projectile;
        float cost = isProjectile ? PROJECTILE_COST : MELEE_COST;

        data.setReverseFlowCapacity(data.getReverseFlowCapacity() - cost);

        LivingEntity attacker = source.getEntity() instanceof LivingEntity le ? le : null;

        if (isProjectile) {
            Projectile proj = (Projectile) source.getDirectEntity();
            Vec3 reflectDir;
            if (attacker != null && attacker.isAlive()) {
                reflectDir = attacker.position().subtract(victim.position()).normalize();
            } else {
                reflectDir = proj.getDeltaMovement().normalize().reverse();
            }
            proj.setPos(victim.getX(), victim.getEyeY(), victim.getZ());
            proj.setDeltaMovement(reflectDir.scale(proj.getDeltaMovement().length() * 1.2));
            try { proj.setOwner(victim); } catch (Throwable ignored) {}
        } else if (attacker != null && attacker.isAlive()) {
            attacker.hurt(victim.level().damageSources().playerAttack(victim), amount);
            Vec3 kb = attacker.position().subtract(victim.position()).normalize().scale(0.6);
            attacker.push(kb.x, kb.y + 0.2, kb.z);
        }

        return true;
    }
}
