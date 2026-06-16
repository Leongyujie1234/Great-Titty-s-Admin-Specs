package com.adminspec.capability;

import com.adminspec.AdminSpecMod;
import com.adminspec.spec.Spec;
import com.adminspec.spec.SpecRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Persistent per-player spec state, stored as a NeoForge data attachment.
 */
public final class PlayerSpecCapability {

    public static final ResourceLocation CAP_ID =
            ResourceLocation.fromNamespaceAndPath(AdminSpecMod.MOD_ID, "player_spec");

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AdminSpecMod.MOD_ID);

    public static final Supplier<AttachmentType<PlayerSpecData>> PLAYER_SPEC =
            ATTACHMENT_TYPES.register("player_spec", () ->
                    AttachmentType.serializable(() -> new PlayerSpecData())
                            .copyOnDeath()
                            .build());

    private PlayerSpecCapability() {}

    /** Get the spec data for a player (client or server). */
    public static PlayerSpecData get(Player player) {
        return player.getData(PLAYER_SPEC);
    }

    // ---- Lifecycle events that keep attachments consistent across death/respawn ----

    public static void onPlayerClone(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof Player newPlayer)) return;
        Player original = event.getOriginal();
        PlayerSpecData oldData = original.getData(PLAYER_SPEC);
        PlayerSpecData newData = newPlayer.getData(PLAYER_SPEC);
        newData.copyFrom(oldData);
    }

    public static void onPlayerRespawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        // Nothing extra — copyOnDeath preserves data.
    }

    public static void onPlayerLoggedIn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PlayerSpecData data = get(player);
        if (data.getSpecId() != null) {
            Spec spec = SpecRegistry.get(data.getSpecId());
            if (spec == null) data.setSpecId(null);
        }
    }

    /**
     * Per-tick update called from a server tick event handler.
     */
    public static void tickPlayer(Player player) {
        if (player.level().isClientSide()) return;
        PlayerSpecData data = get(player);
        if (data.getSpecId() == null) return;
        Spec spec = SpecRegistry.get(data.getSpecId());
        if (spec == null) return;
        data.serverTick(player, spec);
    }
}
