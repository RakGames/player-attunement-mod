package com.zenil.pam.network;

import com.zenil.pam.PAM;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Sent when a lock check denies a player, so the client can show a HUD/chat message without a full sync. */
public record AttunementDeniedPayload(Identifier targetId, String denialMessageKey) implements CustomPacketPayload {

    public static final Type<AttunementDeniedPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(PAM.MOD_ID, "attunement_denied"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementDeniedPayload> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, AttunementDeniedPayload::targetId,
        ByteBufCodecs.STRING_UTF8, AttunementDeniedPayload::denialMessageKey,
        AttunementDeniedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
