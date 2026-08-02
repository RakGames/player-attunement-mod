package com.zenil.pam.network;

import com.zenil.pam.PAM;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server-to-client: shown once per world on a player's first login, if nobody has picked a preset yet. */
public record ShowPresetPickerPayload(Identifier currentPresetId) implements CustomPacketPayload {

    public static final Type<ShowPresetPickerPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(PAM.MOD_ID, "show_preset_picker"));

    public static final StreamCodec<ByteBuf, ShowPresetPickerPayload> STREAM_CODEC =
        Identifier.STREAM_CODEC.map(ShowPresetPickerPayload::new, ShowPresetPickerPayload::currentPresetId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
