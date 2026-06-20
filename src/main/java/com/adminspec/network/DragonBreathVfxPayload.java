package com.adminspec.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DragonBreathVfxPayload(
    int shooterId,
    double eyeX, double eyeY, double eyeZ,
    double lookX, double lookY, double lookZ
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DragonBreathVfxPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("adminspec", "dragon_breath_vfx"));

    public static final StreamCodec<FriendlyByteBuf, DragonBreathVfxPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, DragonBreathVfxPayload>() {
        @Override
        public DragonBreathVfxPayload decode(FriendlyByteBuf buf) {
            return new DragonBreathVfxPayload(
                buf.readVarInt(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
            );
        }

        @Override
        public void encode(FriendlyByteBuf buf, DragonBreathVfxPayload payload) {
            buf.writeVarInt(payload.shooterId());
            buf.writeDouble(payload.eyeX());
            buf.writeDouble(payload.eyeY());
            buf.writeDouble(payload.eyeZ());
            buf.writeDouble(payload.lookX());
            buf.writeDouble(payload.lookY());
            buf.writeDouble(payload.lookZ());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DragonBreathVfxPayload payload, IPayloadContext ctx) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        ctx.enqueueWork(() -> ClientHandler.spawnBreathVfx(payload));
    }

    // Inner class so server-side doesn't classload Minecraft
    public static final class ClientHandler {
        public static void spawnBreathVfx(DragonBreathVfxPayload p) {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level == null) return;

            Vec3 eye  = new Vec3(p.eyeX(), p.eyeY(), p.eyeZ());
            Vec3 look = new Vec3(p.lookX(), p.lookY(), p.lookZ());

            // Spawn a dense sword-qi beam of client particles along the beam path
            for (double d = 0.3; d < 16.0; d += 0.45) {
                Vec3 pos = eye.add(look.scale(d));
                double spread = d * 0.05;

                // Primary: white crit sparkles
                level.addParticle(ParticleTypes.CRIT,
                    pos.x + (Math.random() - 0.5) * spread,
                    pos.y + (Math.random() - 0.5) * spread,
                    pos.z + (Math.random() - 0.5) * spread,
                    look.x * 0.3, look.y * 0.3, look.z * 0.3);

                // Secondary: enchanted hit (blue sparkle)
                level.addParticle(ParticleTypes.ENCHANTED_HIT,
                    pos.x + (Math.random() - 0.5) * spread * 1.5,
                    pos.y + (Math.random() - 0.5) * spread * 1.5,
                    pos.z + (Math.random() - 0.5) * spread * 1.5,
                    (Math.random() - 0.5) * 0.1, (Math.random() - 0.5) * 0.1, (Math.random() - 0.5) * 0.1);

                // Sweep attacks every ~1.5 blocks
                if (d % 1.5 < 0.45) {
                    level.addParticle(ParticleTypes.SWEEP_ATTACK,
                        pos.x, pos.y, pos.z,
                        0, 0, 0);
                }
            }

            // Tip explosion
            Vec3 tip = eye.add(look.scale(16.0));
            for (int i = 0; i < 8; i++) {
                level.addParticle(ParticleTypes.EXPLOSION,
                    tip.x + (Math.random() - 0.5) * 1.5,
                    tip.y + (Math.random() - 0.5) * 1.5,
                    tip.z + (Math.random() - 0.5) * 1.5,
                    0, 0, 0);
            }

            // Sound feedback
            if (mc.player != null) {
                level.playLocalSound(eye.x, eye.y, eye.z,
                    net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.5f, 0.7f, false);
            }
        }
    }
}
