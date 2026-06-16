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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * "Sword Light" — a fast-traveling, instantaneous beam. We model it as a small entity
 * that moves extremely fast (8 blocks/tick) and damages the first entity it hits.
 * Lifetime ~10 ticks (so range ~80 blocks). Damage = 6 hearts (12 HP).
 *
 * Visually it's meant to be "instantaneous, beyond the eye can see" — the projectile is
 * invisible; we just draw a particle streak and apply damage along the path.
 */
public class SwordLightEntity extends Entity {

    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(SwordLightEntity.class, EntityDataSerializers.INT);

    private static final float DAMAGE = 12.0f;          // 6 hearts
    private static final double SPEED = 8.0;            // blocks per tick
    private static final int MAX_LIFE = 10;             // ticks (=> 80-block range)
    private static final double HIT_RADIUS = 1.0;

    private int life = 0;
    private Vec3 direction = Vec3.ZERO;

    public SwordLightEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public SwordLightEntity(EntityType<?> type, Level level, Player owner, Vec3 start, Vec3 direction) {
        this(type, level);
        this.setPos(start);
        this.direction = direction.normalize();
        this.setDeltaMovement(this.direction.scale(SPEED));
        this.entityData.set(OWNER_ID, owner == null ? -1 : owner.getId());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
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
        life++;
        if (life > MAX_LIFE) {
            discard();
            return;
        }

        // Move and check for collisions.
        Vec3 prevPos = this.position();
        Vec3 move = this.getDeltaMovement();
        this.setPos(prevPos.add(move));

        if (!level().isClientSide) {
            // Damage scan along the segment from prevPos -> new pos.
            AABB sweepBox = AABB.ofSize(prevPos.add(move.scale(0.5)), 1.0, 1.0, 1.0).inflate(HIT_RADIUS);
            List<LivingEntity> hits = level().getEntitiesOfClass(LivingEntity.class, sweepBox,
                    e -> e.isAlive() && !e.equals(getOwnerPlayer()) && !e.equals(this));

            Player owner = getOwnerPlayer();
            for (LivingEntity v : hits) {
                AABB vBox = v.getBoundingBox().inflate(HIT_RADIUS);
                if (vBox.contains(this.position())) {
                    DamageSource src = owner != null
                            ? level().damageSources().playerAttack(owner)
                            : level().damageSources().generic();
                    v.hurt(src, DAMAGE);
                    // No knockback — sword light is too fast.
                    onImpact();
                    return;
                }
            }

            // Also stop on solid block.
            if (level().getBlockState(this.blockPosition()).isSolid()) {
                onImpact();
            }
        } else {
            // Client particle streak.
            for (int i = 0; i < 3; i++) {
                level().addParticle(ParticleTypes.END_ROD,
                        getX() + (random.nextDouble() - 0.5) * 0.3,
                        getY() + (random.nextDouble() - 0.5) * 0.3,
                        getZ() + (random.nextDouble() - 0.5) * 0.3,
                        -move.x * 0.05, -move.y * 0.05, -move.z * 0.05);
            }
        }
    }

    private void onImpact() {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 10, 0.1, 0.1, 0.1, 0.05);
        }
        discard();
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
        life = tag.getInt("Life");
        direction = new Vec3(tag.getDouble("DX"), tag.getDouble("DY"), tag.getDouble("DZ"));
        this.setDeltaMovement(direction.scale(SPEED));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Life", life);
        tag.putDouble("DX", direction.x);
        tag.putDouble("DY", direction.y);
        tag.putDouble("DZ", direction.z);
    }
}
