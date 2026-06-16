package com.adminspec.client;

import com.adminspec.AdminSpecMod;
import com.adminspec.entity.GiantHandEntity;
import com.adminspec.entity.ModEntities;
import com.adminspec.entity.SwordLightEntity;
import com.adminspec.entity.YamaChildEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

/**
 * Client-side registration: keybinds, renderers, setup hook.
 */
@EventBusSubscriber(modid = AdminSpecMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    // Key bindings for moves 1-4. Default to keys 1, 2, 3, 4 (above QWERTY row).
    public static final KeyMapping KEY_MOVE_1 = new KeyMapping(
            "key.adminspec.move1", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, InputConstants.KEY_1, "key.categories.adminspec");
    public static final KeyMapping KEY_MOVE_2 = new KeyMapping(
            "key.adminspec.move2", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, InputConstants.KEY_2, "key.categories.adminspec");
    public static final KeyMapping KEY_MOVE_3 = new KeyMapping(
            "key.adminspec.move3", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, InputConstants.KEY_3, "key.categories.adminspec");
    public static final KeyMapping KEY_MOVE_4 = new KeyMapping(
            "key.adminspec.move4", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, InputConstants.KEY_4, "key.categories.adminspec");

    private ClientSetup() {}

    public static void onClientSetup(FMLClientSetupEvent event) {
        // Nothing extra — keybindings are registered via the RegisterKeyMappingsEvent.
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_MOVE_1);
        event.register(KEY_MOVE_2);
        event.register(KEY_MOVE_3);
        event.register(KEY_MOVE_4);
    }

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Use the default entity renderers — Yama Child renders as a zombie, the others use
        // the basic "no model" renderer with particle-only visuals.
        event.registerEntityRenderer(ModEntities.GIANT_HAND.get(), com.adminspec.client.GiantHandRenderer::new);
        event.registerEntityRenderer(ModEntities.SWORD_LIGHT.get(), com.adminspec.client.SwordLightRenderer::new);
        event.registerEntityRenderer(ModEntities.YAMA_CHILD.get(),
                ctx -> new net.minecraft.client.renderer.entity.ZombieRenderer(ctx));
    }
}
