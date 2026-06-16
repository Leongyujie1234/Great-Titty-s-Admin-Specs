package com.adminspec.entity;

import com.adminspec.AdminSpecMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Central registry for entities used by the mod (giant hand, yama child, sword light, etc.)
 */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, AdminSpecMod.MOD_ID);

    // Giant hand — a slow-falling "smasher" entity, no AI, no collision.
    public static final DeferredHolder<EntityType<?>, EntityType<com.adminspec.entity.GiantHandEntity>> GIANT_HAND =
            ENTITIES.register("giant_hand", () -> EntityType.Builder
                    .<com.adminspec.entity.GiantHandEntity>of(com.adminspec.entity.GiantHandEntity::new, MobCategory.MISC)
                    .sized(3.0f, 6.0f)
                    .clientTrackingRange(16)
                    .updateInterval(3)
                    .fireImmune()
                    .build("giant_hand"));

    // Yama child — baby zombie-like suicide bomber.
    public static final DeferredHolder<EntityType<?>, EntityType<com.adminspec.entity.YamaChildEntity>> YAMA_CHILD =
            ENTITIES.register("yama_child", () -> EntityType.Builder
                    .<com.adminspec.entity.YamaChildEntity>of(com.adminspec.entity.YamaChildEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.0f)
                    .clientTrackingRange(16)
                    .build("yama_child"));

    // Sword light — fast traveling, instantaneous beam projectile.
    public static final DeferredHolder<EntityType<?>, EntityType<com.adminspec.entity.SwordLightEntity>> SWORD_LIGHT =
            ENTITIES.register("sword_light", () -> EntityType.Builder
                    .<com.adminspec.entity.SwordLightEntity>of(com.adminspec.entity.SwordLightEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(24)
                    .updateInterval(1)
                    .build("sword_light"));

    /** Register entity attributes (needed for Yama Child because we customize them). */
    public static void onRegisterAttributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(YAMA_CHILD.get(), com.adminspec.entity.YamaChildEntity.createYamaAttributes().build());
    }
}
