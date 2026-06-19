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

import com.adminspec.client.ClientDragonFormState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DragonFormPayload(boolean active) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<DragonFormPayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"adminspec", (String)"dragon_form"));
    public static final StreamCodec<FriendlyByteBuf, DragonFormPayload> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.BOOL, DragonFormPayload::active, DragonFormPayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DragonFormPayload payload, IPayloadContext ctx) {
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }
        ctx.enqueueWork(() -> ClientDragonFormState.setActive(payload.active()));
    }
}

