package com.adminspec.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * "Giant Hand" — a very slow falling entity that, on contact with the ground,
 * smashes down dealing AoE damage to entities below it and creating a shockwave of particles.
 *
 * Lifetime: 6 seconds max. The hand falls slowly (~ -0.25 blocks/tick vertical) and on landing
 * deals 12 hearts of damage in a 5-block radius, with knockback.
 */
public class GiantHandEntity extends Entity {

    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(GiantHandEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(GiantHandEntity.class, EntityDataSerializers.INT);

    private int lifetimeTicks = 6 * 20; // 6 seconds
    private boolean hasLanded = false;
    private int landedTicks = 0;
    private float damage = 24.0f; // 12 hearts
    private float radius = 5.0f;

    public GiantHandEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(true); // we manage falling manually
    }

    public GiantHandEntity(EntityType<?> type, Level level, Player owner, Vec3 startPos) {
        this(type, level);
        this.setPos(startPos);
        this.entityData.set(OWNER_ID, owner == null ? -1 : owner.getId());
        this.entityData.set(SCALE, 1.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SCALE, 1.0f);
        builder.define(OWNER_ID, -1);
    }

    public float getScale() {
        return entityData.get(SCALE);
    }

    public Player getOwnerPlayer() {
        int id = entityData.get(OWNER_ID);
        if (id < 0 || level().isClientSide) return null;
        Entity e = level().getEntity(id);
        return e instanceof Player p ? p : null;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            lifetimeTicks--;
            if (lifetimeTicks <= 0) {
                discard();
                return;
            }

            if (!hasLanded) {
                // Very slow descent — 0.25 blocks/tick (~5 blocks/sec).
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.025, 0));
                this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

                // Check if we hit the ground.
                if (this.onGround() || this.getY() < level().getMinBuildHeight()) {
                    land();
                }
            } else {
                landedTicks++;
                // Hand sits on ground briefly for the visual, then disappears.
                if (landedTicks > 10) discard();
            }
        } else {
            // Client: spawn dust particles falling from the hand.
            if (random.nextFloat() < 0.3f) {
                level().addParticle(ParticleTypes.CLOUD,
                        getX() + (random.nextDouble() - 0.5) * 3.0,
                        getY() + random.nextDouble() * 6.0,
                        getZ() + (random.nextDouble() - 0.5) * 3.0,
                        0, -0.05, 0);
            }
        }
    }

    private void land() {
        hasLanded = true;
        // AoE damage in radius below the hand.
        AABB box = AABB.ofSize(this.position(), radius * 2, 4.0, radius * 2);
        List<LivingEntity> victims = level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && !e.equals(getOwnerPlayer()));

        Player owner = getOwnerPlayer();
        DamageSource src = owner != null
                ? level().damageSources().playerAttack(owner)
                : level().damageSources().generic();

        for (LivingEntity v : victims) {
            v.hurt(src, damage);
            // Strong knockback downward+outward.
            Vec3 knock = v.position().subtract(this.position()).normalize().add(0, -0.4, 0).scale(1.2);
            v.push(knock.x, knock.y, knock.z);
        }

        // Big particle burst + camera shake.
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 4, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.3, getZ(), 30, 2.5, 0.3, 2.5, 0.05);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        lifetimeTicks = tag.getInt("Life");
        hasLanded = tag.getBoolean("Landed");
        landedTicks = tag.getInt("LandedTicks");
        damage = tag.getFloat("Damage");
        radius = tag.getFloat("Radius");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Life", lifetimeTicks);
        tag.putBoolean("Landed", hasLanded);
        tag.putInt("LandedTicks", landedTicks);
        tag.putFloat("Damage", damage);
        tag.putFloat("Radius", radius);
    }
}
