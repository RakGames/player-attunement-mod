package com.zenil.pam.event;

import com.zenil.pam.PAM;
import com.zenil.pam.api.AttunementApi;
import com.zenil.pam.api.AttunementCheckEvent;
import com.zenil.pam.attunement.AttunementDefinition;
import com.zenil.pam.network.PAMNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/** Gates right-click interaction with locked blocks (beacons, ender chests, shulker boxes, ...). */
@EventBusSubscriber(modid = PAM.MOD_ID)
public final class BlockInteractLockHandler {
    private BlockInteractLockHandler() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = player.level().getBlockState(pos);
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        List<AttunementDefinition> guards = PAM.ATTUNEMENT_MANAGER.lockingBlock(blockId);
        if (guards.isEmpty()) return;

        if (!AttunementApi.evaluate(player, blockId, AttunementCheckEvent.LockType.BLOCK, guards)) {
            event.setCanceled(true);
            event.setUseBlock(TriState.FALSE);
            PAMNetworking.sendDenied(player, blockId, guards.get(0).denialMessage());
        }
    }
}
