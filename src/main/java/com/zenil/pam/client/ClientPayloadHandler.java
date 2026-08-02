package com.zenil.pam.client;

import com.zenil.pam.network.AttunementDeniedPayload;
import com.zenil.pam.network.ShowPresetPickerPayload;
import com.zenil.pam.network.SyncAttunementsPayload;
import com.zenil.pam.preset.PAMPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Only invoked client-side, via method references registered from common code in {@code PAMNetworking}. */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handleSync(SyncAttunementsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientAttunementCache.set(payload.unlocked()));
    }

    public static void handleDenied(AttunementDeniedPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.sendOverlayMessage(Component.translatable(payload.denialMessageKey()));
            }
        });
    }

    public static void handleShowPresetPicker(ShowPresetPickerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            PAMPreset current = PAMPreset.byId(payload.currentPresetId()).orElse(PAMPreset.VANILLA_PLUS);
            Minecraft.getInstance().setScreenAndShow(new PAMFirstJoinScreen(current));
        });
    }
}
