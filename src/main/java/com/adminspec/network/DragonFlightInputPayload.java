package com.adminspec.network;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DragonFlightInputPayload(
    boolean jumping,
    boolean sneaking,
    float forward,
    float strafe
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DragonFlightInputPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("adminspec", "dragon_flight_input"));

    public static final StreamCodec<FriendlyByteBuf, DragonFlightInputPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,  DragonFlightInputPayload::jumping,
                    ByteBufCodecs.BOOL,  DragonFlightInputPayload::sneaking,
                    ByteBufCodecs.FLOAT, DragonFlightInputPayload::forward,
                    ByteBufCodecs.FLOAT, DragonFlightInputPayload::strafe,
                    DragonFlightInputPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DragonFlightInputPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player p = ctx.player();
            if (!(p instanceof ServerPlayer)) return;
            PlayerSpecData data = PlayerSpecCapability.get(p);
            data.setDragonJumping(payload.jumping());
            data.setDragonSneaking(payload.sneaking());
            data.setDragonForward(payload.forward());
            data.setDragonStrafe(payload.strafe());
        });
    }
}
