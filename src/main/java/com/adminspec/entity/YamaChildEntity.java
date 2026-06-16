package com.adminspec.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Yama Child — a baby-zombie-like mob (visually identical for now) that walks toward the nearest
 * enemy and self-detonates with 2x TNT damage but same blast radius/destruction as TNT.
 */
public class YamaChildEntity extends Zombie {

    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(YamaChildEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FUSE_LIT =
            SynchedEntityData.defineId(YamaChildEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int FUSE_TICKS = 20;       // 1 second glow before boom
    private static final float TNT_DAMAGE = 24.0f;   // vanilla TNT ~ 24 @ center; we double to 48
    private static final float TNT_RADIUS = 4.0f;     // same as TNT

    private int fuseTimer = -1;
    private int lifetimeTicks = 0;
    private static final int MAX_LIFETIME = 30 * 20; // 30s then despawn

    public YamaChildEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        this.setBaby(true);
        this.setPersistenceRequired();
    }

    public YamaChildEntity(EntityType<? extends Zombie> type, Level level, Player owner) {
        this(type, level);
        this.entityData.set(OWNER_ID, owner == null ? -1 : owner.getId());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_ID, -1);
        builder.define(FUSE_LIT, false);
    }

    public Player getOwnerPlayer() {
        int id = entityData.get(OWNER_ID);
        if (id < 0 || level().isClientSide) return null;
        Entity e = level().getEntity(id);
        return e instanceof Player p ? p : null;
    }

    /** Custom attributes for the Yama Child. Renamed to avoid clash with Zombie.createAttributes(). */
    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createYamaAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35) // faster than vanilla zombie
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        // Targets anything that isn't the owner (player or mob).
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                e -> !(e instanceof YamaChildEntity) && !e.equals(getOwnerPlayer()) && e.isAlive()));
    }

    @Override
    public void tick() {
        super.tick();
        lifetimeTicks++;
        if (lifetimeTicks > MAX_LIFETIME) {
            discard();
            return;
        }

        if (fuseTimer > 0) {
            fuseTimer--;
            this.entityData.set(FUSE_LIT, true);
            // Charging particles.
            if (level().isClientSide && random.nextFloat() < 0.4f) {
                level().addParticle(ParticleTypes.SMOKE,
                        getX() + (random.nextDouble() - 0.5) * 0.4,
                        getY() + random.nextDouble() * 1.0,
                        getZ() + (random.nextDouble() - 0.5) * 0.4,
                        0, 0.1, 0);
            }
            if (fuseTimer == 0) {
                detonate();
            }
        } else {
            // Auto-detonate when close to a target.
            LivingEntity target = getTarget();
            if (target != null && this.distanceToSqr(target) < 2.5 * 2.5) {
                fuseTimer = FUSE_TICKS;
            }
        }
    }

    private void detonate() {
        if (level().isClientSide) return;

        Player owner = getOwnerPlayer();
        DamageSource src = owner != null
                ? level().damageSources().playerAttack(owner)
                : level().damageSources().mobAttack(this);

        AABB box = AABB.ofSize(this.position(), TNT_RADIUS * 2, TNT_RADIUS * 2, TNT_RADIUS * 2);
        List<LivingEntity> victims = level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && !e.equals(this) && !e.equals(owner));

        for (LivingEntity v : victims) {
            double dist = v.position().distanceTo(this.position());
            double falloff = Math.max(0.0, 1.0 - dist / TNT_RADIUS);
            float dmg = (float) (TNT_DAMAGE * 2.0 * falloff); // 2x TNT damage
            v.hurt(src, dmg);
            // Knockback away from blast.
            Vec3 kb = v.position().subtract(this.position()).normalize().scale(falloff * 1.5);
            v.push(kb.x, kb.y + 0.3, kb.z);
        }

        // Same explosion visual + block destruction as vanilla TNT (power = 4.0).
        level().explode(this, this.getX(), this.getY() + 0.5, this.getZ(), 4.0f,
                Level.ExplosionInteraction.TNT);

        // Sound.
        level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.5f, 0.8f);

        // Particles.
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 0.5, getZ(),
                    2, 0, 0, 0, 0);
        }

        this.discard();
    }

    @Override
    public boolean isBaby() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Fuse", fuseTimer);
        tag.putInt("Life", lifetimeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fuseTimer = tag.getInt("Fuse");
        lifetimeTicks = tag.getInt("Life");
    }
}
