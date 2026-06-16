package com.adminspec.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> Client: snapshot of a player's spec state (used by the renderer to draw outlines).
 *
 * Sent to all tracking clients whenever spec state changes; the client stores the latest snapshot
 * keyed by player UUID in {@link com.adminspec.client.ClientSpecState}.
 */
public record SpecStatePayload(
        java.util.UUID playerId,
        boolean reverseFlowActive,
        boolean yamaActive,
        float reverseFlowCapacity
) implements CustomPacketPayload {

    public static final Type<SpecStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("adminspec", "spec_state"));

    public static final StreamCodec<FriendlyByteBuf, SpecStatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    net.minecraft.core.UUIDUtil.STREAM_CODEC, SpecStatePayload::playerId,
                    ByteBufCodecs.BOOL, SpecStatePayload::reverseFlowActive,
                    ByteBufCodecs.BOOL, SpecStatePayload::yamaActive,
                    ByteBufCodecs.FLOAT, SpecStatePayload::reverseFlowCapacity,
                    SpecStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SpecStatePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            com.adminspec.client.ClientSpecState.update(payload);
        });
    }
}
