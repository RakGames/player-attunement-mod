package com.zenil.pam.network;

import com.zenil.pam.PAM;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client-to-server: the player's choice from the first-join preset picker. */
public record SelectPresetPayload(Identifier presetId) implements CustomPacketPayload {

    public static final Type<SelectPresetPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(PAM.MOD_ID, "select_preset"));

    public static final StreamCodec<ByteBuf, SelectPresetPayload> STREAM_CODEC =
        Identifier.STREAM_CODEC.map(SelectPresetPayload::new, SelectPresetPayload::presetId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
