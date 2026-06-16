package com.adminspec;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.command.AdminSpecCommand;
import com.adminspec.entity.ModEntities;
import com.adminspec.moves.ModMoves;
import com.adminspec.spec.SpecRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(AdminSpecMod.MOD_ID)
public class AdminSpecMod {
    public static final String MOD_ID = "adminspec";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AdminSpecMod(IEventBus modBus) {
        ModEntities.ENTITIES.register(modBus);
        ModMoves.MOVES.register(modBus);
        PlayerSpecCapability.ATTACHMENT_TYPES.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(com.adminspec.entity.ModEntities::onRegisterAttributes);

        // Client-side registration on the mod bus.
        modBus.addListener(com.adminspec.client.ClientSetup::onClientSetup);
        modBus.addListener(com.adminspec.client.ClientSetup::onRegisterKeyMappings);
        modBus.addListener(com.adminspec.client.ClientSetup::onRegisterRenderers);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(PlayerSpecCapability::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(PlayerSpecCapability::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(PlayerSpecCapability::onPlayerLoggedIn);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SpecRegistry.registerDefaults();
            LOGGER.info("Admin Spec mod: registered {} specs.", SpecRegistry.size());
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        AdminSpecCommand.register(event.getDispatcher());
    }
}
