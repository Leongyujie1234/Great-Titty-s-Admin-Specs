/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.event.entity.ProjectileImpactEvent
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Pre
 */
package com.adminspec.capability;

import com.adminspec.capability.BlockRecoveryManager;
import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.network.SpecStatePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;

@EventBusSubscriber(modid="adminspec", bus=EventBusSubscriber.Bus.GAME)
public final class SpecEvents {
    private static int syncTickCounter = 0;

    private SpecEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        MinecraftServer server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerSpecCapability.tickPlayer((Player)player);
        }
        for (ServerLevel level : server.getAllLevels()) {
            BlockRecoveryManager mgr = BlockRecoveryManager.get(level);
            if (mgr == null) continue;
            mgr.tick(level);
        }
        if (++syncTickCounter >= 5) {
            syncTickCounter = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PlayerSpecData data = PlayerSpecCapability.get((Player)player);
                // Sync if player has any active spec state (reverse flow OR dragon form)
                if (!data.isReverseFlowActive() && !data.isDragonFormActive()
                    && data.getReverseFlowCapacity() >= 1.0f) continue;
                SpecStatePayload.broadcast((Player)player);
            }
        }
    }

    /**
     * Post-tick: override vanilla creative flight physics with our custom dragon
     * velocity.  Vanilla flight runs inside ServerPlayer.tick() and would otherwise
     * overwrite the values we set in AncientSwordDragonTransformationMove.tick().
     * By re-applying here we guarantee our velocity sticks.
     */
    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            PlayerSpecData data = PlayerSpecCapability.get(sp);
            if (!data.isDragonFormActive()) continue;
            if (data.getDragonFormTicks() < 20) {
                // Force upward launch during transition
                double progress = (double) data.getDragonFormTicks() / 20.0;
                double up = 1.3 * (1.0 - progress);
                sp.setDeltaMovement(sp.getDeltaMovement().x, up, sp.getDeltaMovement().z);
            } else {
                applyDragonFlightVelocity(sp, data);
            }
            sp.hurtMarked = true;
        }
    }

    private static void applyDragonFlightVelocity(ServerPlayer sp, PlayerSpecData data) {
        // Pull flight constants (must match AncientSwordDragonTransformationMove)
        double FLIGHT_ACCEL    = 0.08;
        double FLIGHT_MAX_SPEED = 1.2;
        double FLIGHT_FRICTION  = 0.90;
        double VERT_ACCEL       = 0.06;

        Vec3 velocity = sp.getDeltaMovement();
        Vec3 look = sp.getLookAngle();
        Vec3 moveDir = Vec3.ZERO;

        float forward = data.getDragonForward();
        float strafe  = data.getDragonStrafe();

        if (forward > 0.01f) {
            moveDir = moveDir.add(look);
        } else if (forward < -0.01f) {
            moveDir = moveDir.subtract(look);
        }

        if (Math.abs(strafe) > 0.01f) {
            Vec3 right = new Vec3(look.z, 0, -look.x).normalize();
            if (strafe > 0f) {
                moveDir = moveDir.subtract(right);  // strafe right
            } else {
                moveDir = moveDir.add(right);       // strafe left
            }
        }

        // Vertical: jump = ascend, sneak = descend
        if (data.isDragonJumping()) {
            moveDir = moveDir.add(0, VERT_ACCEL / FLIGHT_ACCEL, 0);
        } else if (data.isDragonSneaking()) {
            moveDir = moveDir.subtract(0, VERT_ACCEL / FLIGHT_ACCEL, 0);
        }

        // Apply acceleration
        if (moveDir.lengthSqr() > 1.0E-6) {
            Vec3 accel = moveDir.normalize().scale(FLIGHT_ACCEL);
            velocity = velocity.add(accel);
        }

        // Friction + speed cap
        velocity = velocity.scale(FLIGHT_FRICTION);
        double speed = velocity.length();
        if (speed > FLIGHT_MAX_SPEED) {
            velocity = velocity.scale(FLIGHT_MAX_SPEED / speed);
        }

        sp.setDeltaMovement(velocity);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        if (player.level().isClientSide) {
            return;
        }
        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (data.isSwordEscapeDashing()) {
            event.setCanceled(true);
            return;
        }
        if (data.isReverseFlowActive()) {
            LivingEntity le;
            LivingEntity attacker;
            float amount = event.getAmount();
            DamageSource source = event.getSource();
            event.setCanceled(true);
            float cost = Math.max(0.02f, amount * 0.005f);
            float newCap = data.getReverseFlowCapacity() - cost;
            data.setReverseFlowCapacity(newCap);
            if (newCap <= 0.0f) {
                data.setReverseFlowCapacity(0.0f);
                data.setReverseFlowActive(false);
                player.sendSystemMessage((Component)Component.literal((String)"\u00a7b[Reverse Flow Protection Seal] \u00a7cThe river has run dry. Seal disengaged."));
                SpecStatePayload.broadcast(player);
                return;
            }
            Entity entity = source.getEntity();
            LivingEntity livingEntity2 = attacker = entity instanceof LivingEntity ? (le = (LivingEntity)entity) : null;
            if (attacker != null && attacker.isAlive() && !attacker.equals((Object)player)) {
                attacker.hurt(player.level().damageSources().playerAttack(player), amount);
            }
            SpecStatePayload.broadcast(player);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        LivingEntity le;
        Projectile proj = event.getProjectile();
        HitResult hit = event.getRayTraceResult();
        if (hit.getType() != HitResult.Type.ENTITY) {
            return;
        }
        Entity entity = proj.level().getEntity(((EntityHitResult)hit).getEntity().getId());
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (player.level().isClientSide) {
            return;
        }
        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (!data.isReverseFlowActive()) {
            return;
        }
        event.setCanceled(true);
        float cost = 0.03f;
        float newCap = data.getReverseFlowCapacity() - cost;
        data.setReverseFlowCapacity(newCap);
        if (newCap <= 0.0f) {
            data.setReverseFlowCapacity(0.0f);
            data.setReverseFlowActive(false);
            player.sendSystemMessage((Component)Component.literal((String)"\u00a7b[Reverse Flow Protection Seal] \u00a7cThe river has run dry. Seal disengaged."));
            SpecStatePayload.broadcast(player);
            return;
        }
        Entity entity2 = proj.getOwner();
        LivingEntity shooter = entity2 instanceof LivingEntity ? (le = (LivingEntity)entity2) : null;
        Vec3 reflectDir = shooter != null && shooter.isAlive() && !shooter.equals((Object)player) ? shooter.position().subtract(player.position()).normalize() : proj.getDeltaMovement().normalize().reverse();
        proj.setPos(player.getX(), player.getEyeY(), player.getZ());
        Vec3 oldVel = proj.getDeltaMovement();
        double speed = oldVel.length() * 1.5;
        if (speed < 1.0) {
            speed = 2.0;
        }
        proj.setDeltaMovement(reflectDir.scale(speed));
        try {
            proj.setOwner((Entity)player);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        SpecStatePayload.broadcast(player);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player != null) {
            PlayerSpecData data = PlayerSpecCapability.get(player);
            if (data.isDragonFormActive()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player != null) {
            PlayerSpecData data = PlayerSpecCapability.get(player);
            if (data.isDragonFormActive()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player != null) {
            PlayerSpecData data = PlayerSpecCapability.get(player);
            if (data.isDragonFormActive()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player != null) {
            PlayerSpecData data = PlayerSpecCapability.get(player);
            if (data.isDragonFormActive()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player != null) {
            PlayerSpecData data = PlayerSpecCapability.get(player);
            if (data.isDragonFormActive()) {
                event.setCanceled(true);
            }
        }
    }
}

