package com.adminspec.network;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> Server: "I pressed move key at index N (0-based)."
 */
public record ActivateMovePayload(int moveIndex) implements CustomPacketPayload {

    public static final Type<ActivateMovePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("adminspec", "activate_move"));

    public static final StreamCodec<FriendlyByteBuf, ActivateMovePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ActivateMovePayload::moveIndex,
                    ActivateMovePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ActivateMovePayload payload, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        ctx.enqueueWork(() -> {
            PlayerSpecData data = PlayerSpecCapability.get(player);
            // 0-based -> 1-based index.
            data.activateMove(player, payload.moveIndex() + 1, true);
        });
    }
}
