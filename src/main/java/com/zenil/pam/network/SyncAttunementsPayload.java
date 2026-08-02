package com.zenil.pam.network;

import com.zenil.pam.PAM;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Full replace-sync of a player's unlocked attunement set. Sent on login, grant, revoke, and clear. */
public record SyncAttunementsPayload(List<Identifier> unlocked) implements CustomPacketPayload {

    public static final Type<SyncAttunementsPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(PAM.MOD_ID, "sync_attunements"));

    // Identifier.STREAM_CODEC is fixed to plain ByteBuf; RegistryFriendlyByteBuf is a subtype,
    // so this still satisfies PayloadRegistrar's `StreamCodec<? super RegistryFriendlyByteBuf, T>`.
    public static final StreamCodec<ByteBuf, SyncAttunementsPayload> STREAM_CODEC =
        Identifier.STREAM_CODEC.apply(ByteBufCodecs.list())
            .map(SyncAttunementsPayload::new, SyncAttunementsPayload::unlocked);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
