/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.fml.loading.FMLEnvironment
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.adminspec.network;

import com.adminspec.client.ClientBeamSpawner;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SwordEscapeBeamPayload(double startX, double startY, double startZ, double endX, double endY, double endZ) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SwordEscapeBeamPayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"adminspec", (String)"sword_escape_beam"));
    public static final StreamCodec<FriendlyByteBuf, SwordEscapeBeamPayload> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.DOUBLE, SwordEscapeBeamPayload::startX, (StreamCodec)ByteBufCodecs.DOUBLE, SwordEscapeBeamPayload::startY, (StreamCodec)ByteBufCodecs.DOUBLE, SwordEscapeBeamPayload::startZ, (StreamCodec)ByteBufCodecs.DOUBLE, SwordEscapeBeamPayload::endX, (StreamCodec)ByteBufCodecs.DOUBLE, SwordEscapeBeamPayload::endY, (StreamCodec)ByteBufCodecs.DOUBLE, SwordEscapeBeamPayload::endZ, SwordEscapeBeamPayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SwordEscapeBeamPayload payload, IPayloadContext ctx) {
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }
        ctx.enqueueWork(() -> ClientBeamSpawner.spawnBeam(payload.startX(), payload.startY(), payload.startZ(), payload.endX(), payload.endY(), payload.endZ()));
    }
}

