package com.zenil.pam.event;

import com.zenil.pam.PAM;
import com.zenil.pam.api.AttunementApi;
import com.zenil.pam.api.AttunementCheckEvent;
import com.zenil.pam.attunement.AttunementDefinition;
import com.zenil.pam.network.PAMNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

import java.util.List;

/** Gates Nether/End/modded portal travel behind attunement checks. */
@EventBusSubscriber(modid = PAM.MOD_ID)
public final class DimensionLockHandler {
    private DimensionLockHandler() {}

    @SubscribeEvent
    public static void onDimensionTravel(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Identifier dimensionId = event.getDimension().identifier();
        List<AttunementDefinition> guards = PAM.ATTUNEMENT_MANAGER.lockingDimension(dimensionId);
        if (guards.isEmpty()) return;

        if (!AttunementApi.evaluate(player, dimensionId, AttunementCheckEvent.LockType.DIMENSION, guards)) {
            event.setCanceled(true);
            String denialMessage = guards.get(0).denialMessage();
            player.sendSystemMessage(Component.translatable(denialMessage));
            PAMNetworking.sendDenied(player, dimensionId, denialMessage);

            if (event.getDimension().equals(Level.END)) {
                bounceToBed(player);
            }
        }
    }

    /**
     * Sends a denied End traveler back to their bed (or world spawn, if none). This is called
     * from inside the original {@code ServerPlayer.teleport()} call (via
     * {@code CommonHooks.onTravelToDimension}), so the nested {@code teleport()} below reenters
     * that in-progress call. This is safe only because {@code teleport()}'s entire body is
     * gated behind that one hook check: once it returns canceled, the outer call does nothing
     * but return {@code null}, so the nested call here is the only one that mutates player
     * state. Do not add logic to the outer call path without re-checking this.
     */
    private static void bounceToBed(ServerPlayer player) {
        player.teleport(player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING));
    }
}
