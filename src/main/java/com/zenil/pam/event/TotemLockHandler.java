package com.zenil.pam.event;

import com.zenil.pam.PAM;
import com.zenil.pam.api.AttunementApi;
import com.zenil.pam.api.AttunementCheckEvent;
import com.zenil.pam.attunement.AttunementDefinition;
import com.zenil.pam.network.PAMNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;

import java.util.List;

/**
 * Gates Totem of Undying activation (reuses the item-lock category). Canceling this event lets
 * the fatal damage go through instead of being negated by the totem.
 */
@EventBusSubscriber(modid = PAM.MOD_ID)
public final class TotemLockHandler {
    private TotemLockHandler() {}

    @SubscribeEvent
    public static void onUseTotem(LivingUseTotemEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Identifier itemId = BuiltInRegistries.ITEM.getKey(event.getTotem().getItem());
        List<AttunementDefinition> guards = PAM.ATTUNEMENT_MANAGER.lockingItem(itemId);
        if (guards.isEmpty()) return;

        if (!AttunementApi.evaluate(player, itemId, AttunementCheckEvent.LockType.ITEM, guards)) {
            event.setCanceled(true);
            PAMNetworking.sendDenied(player, itemId, guards.get(0).denialMessage());
        }
    }
}
