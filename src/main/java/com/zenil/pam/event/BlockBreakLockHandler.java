package com.zenil.pam.event;

import com.zenil.pam.PAM;
import com.zenil.pam.api.AttunementApi;
import com.zenil.pam.api.AttunementCheckEvent;
import com.zenil.pam.attunement.AttunementDefinition;
import com.zenil.pam.network.PAMNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.List;

/** Gates mining/tool use: breaking a block while holding a locked tool is denied (reuses the item-lock category). */
@EventBusSubscriber(modid = PAM.MOD_ID)
public final class BlockBreakLockHandler {
    private BlockBreakLockHandler() {}

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty()) return;

        Identifier itemId = BuiltInRegistries.ITEM.getKey(tool.getItem());
        List<AttunementDefinition> guards = PAM.ATTUNEMENT_MANAGER.lockingItem(itemId);
        if (guards.isEmpty()) return;

        if (!AttunementApi.evaluate(player, itemId, AttunementCheckEvent.LockType.ITEM, guards)) {
            event.setCanceled(true);
            PAMNetworking.sendDenied(player, itemId, guards.get(0).denialMessage());
        }
    }
}
