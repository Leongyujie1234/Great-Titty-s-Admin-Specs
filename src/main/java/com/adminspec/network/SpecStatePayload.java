/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.UUIDUtil
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.fml.loading.FMLEnvironment
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.adminspec.network;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.client.ClientSpecState;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpecStatePayload(UUID playerId, boolean reverseFlowActive, float reverseFlowCapacity, boolean dragonFormActive) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SpecStatePayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"adminspec", (String)"spec_state"));
    public static final StreamCodec<FriendlyByteBuf, SpecStatePayload> STREAM_CODEC = StreamCodec.composite((StreamCodec)UUIDUtil.STREAM_CODEC, SpecStatePayload::playerId, (StreamCodec)ByteBufCodecs.BOOL, SpecStatePayload::reverseFlowActive, (StreamCodec)ByteBufCodecs.FLOAT, SpecStatePayload::reverseFlowCapacity, (StreamCodec)ByteBufCodecs.BOOL, SpecStatePayload::dragonFormActive, SpecStatePayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SpecStatePayload payload, IPayloadContext ctx) {
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }
        ctx.enqueueWork(() -> ClientSpecState.update(payload));
    }

    public static void broadcast(Player player) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)player;
        PlayerSpecData data = PlayerSpecCapability.get(player);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf((Entity)sp, (CustomPacketPayload)new SpecStatePayload(player.getUUID(), data.isReverseFlowActive(), data.getReverseFlowCapacity(), data.isDragonFormActive()), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }
}

