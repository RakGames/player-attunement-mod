package com.zenil.pam.network;

import com.zenil.pam.PAM;
import com.zenil.pam.attunement.AttunementData;
import com.zenil.pam.attunement.PAMAttachments;
import com.zenil.pam.client.ClientPayloadHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

public final class PAMNetworking {
    private PAMNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncAttunementsPayload.TYPE, SyncAttunementsPayload.STREAM_CODEC, ClientPayloadHandler::handleSync);
        registrar.playToClient(AttunementDeniedPayload.TYPE, AttunementDeniedPayload.STREAM_CODEC, ClientPayloadHandler::handleDenied);
        registrar.playToClient(ShowPresetPickerPayload.TYPE, ShowPresetPickerPayload.STREAM_CODEC, ClientPayloadHandler::handleShowPresetPicker);
        registrar.playToServer(SelectPresetPayload.TYPE, SelectPresetPayload.STREAM_CODEC, PAMNetworking::handleSelectPreset);
    }

    public static void syncTo(ServerPlayer player) {
        AttunementData data = player.getData(PAMAttachments.ATTUNEMENTS);
        PacketDistributor.sendToPlayer(player, new SyncAttunementsPayload(List.copyOf(data.view())));
    }

    public static void sendDenied(ServerPlayer player, Identifier targetId, String denialMessageKey) {
        PacketDistributor.sendToPlayer(player, new AttunementDeniedPayload(targetId, denialMessageKey));
    }

    public static void sendShowPresetPicker(ServerPlayer player, Identifier currentPresetId) {
        PacketDistributor.sendToPlayer(player, new ShowPresetPickerPayload(currentPresetId));
    }

    private static void handleSelectPreset(SelectPresetPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PAM.PRESET_MANAGER.activateAndPersist(player.level().getServer(), payload.presetId());
            }
        });
    }
}
