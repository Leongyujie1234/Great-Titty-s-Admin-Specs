package com.adminspec.network;

import com.adminspec.AdminSpecMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers all custom payloads (client<->server) for the mod.
 */
@EventBusSubscriber(modid = AdminSpecMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModPayloads {

    private ModPayloads() {}

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("1");
        reg.playToServer(ActivateMovePayload.TYPE, ActivateMovePayload.STREAM_CODEC,
                ActivateMovePayload::handle);
        reg.playToClient(SpecStatePayload.TYPE, SpecStatePayload.STREAM_CODEC,
                SpecStatePayload::handle);
    }
}
