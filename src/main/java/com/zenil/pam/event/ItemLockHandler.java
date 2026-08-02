package com.zenil.pam.event;

import com.zenil.pam.PAM;
import com.zenil.pam.api.AttunementApi;
import com.zenil.pam.api.AttunementCheckEvent;
import com.zenil.pam.attunement.AttunementDefinition;
import com.zenil.pam.network.PAMNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/** Gates equipping gated armor/Elytra and right-click/use of gated items. */
@EventBusSubscriber(modid = PAM.MOD_ID)
public final class ItemLockHandler {
    private ItemLockHandler() {}

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Elytra shares the CHEST slot with chestplates, both HUMANOID_ARMOR type; ANIMAL_ARMOR (horses/wolves) is untouched.
        if (event.getSlot().getType() != EquipmentSlot.Type.HUMANOID_ARMOR) return;

        ItemStack equipped = event.getTo();
        if (equipped.isEmpty()) return;

        Identifier itemId = BuiltInRegistries.ITEM.getKey(equipped.getItem());
        if (!checkAndNotify(player, itemId)) {
            // LivingEquipmentChangeEvent isn't cancellable, so revert manually.
            player.setItemSlot(event.getSlot(), event.getFrom());
            // Equippable#swapWithEquipmentSlot copies rather than moves the source stack for
            // creative players (they keep their original), but truly moves it for survival
            // players. Only give the item back for survival — a creative player never lost it,
            // so returning one here would duplicate it.
            if (!player.isCreative()) {
                player.getInventory().placeItemBackInInventory(equipped);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;

        Identifier itemId = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!checkAndNotify(player, itemId)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    private static boolean checkAndNotify(ServerPlayer player, Identifier itemId) {
        List<AttunementDefinition> guards = PAM.ATTUNEMENT_MANAGER.lockingItem(itemId);
        if (guards.isEmpty()) return true;

        boolean allowed = AttunementApi.evaluate(player, itemId, AttunementCheckEvent.LockType.ITEM, guards);
        if (!allowed) {
            PAMNetworking.sendDenied(player, itemId, guards.get(0).denialMessage());
        }
        return allowed;
    }
}
