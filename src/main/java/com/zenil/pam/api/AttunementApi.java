package com.zenil.pam.api;

import com.zenil.pam.attunement.AttunementData;
import com.zenil.pam.attunement.AttunementDefinition;
import com.zenil.pam.attunement.PAMAttachments;
import com.zenil.pam.network.PAMNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Set;

/** Public entry point for other mods, KubeJS, and CraftTweaker to query and mutate attunements. */
public final class AttunementApi {
    private AttunementApi() {}

    public static boolean has(ServerPlayer player, Identifier attunementId) {
        return player.getData(PAMAttachments.ATTUNEMENTS).has(attunementId);
    }

    public static boolean grant(ServerPlayer player, Identifier attunementId) {
        AttunementData data = player.getData(PAMAttachments.ATTUNEMENTS);
        boolean changed = data.grant(attunementId);
        if (changed) {
            NeoForge.EVENT_BUS.post(new AttunementUnlockEvent(player, attunementId));
            PAMNetworking.syncTo(player);
        }
        return changed;
    }

    public static boolean revoke(ServerPlayer player, Identifier attunementId) {
        AttunementData data = player.getData(PAMAttachments.ATTUNEMENTS);
        boolean changed = data.revoke(attunementId);
        if (changed) {
            PAMNetworking.syncTo(player);
        }
        return changed;
    }

    public static void clear(ServerPlayer player) {
        player.getData(PAMAttachments.ATTUNEMENTS).clear();
        PAMNetworking.syncTo(player);
    }

    public static Set<Identifier> unlocked(ServerPlayer player) {
        return player.getData(PAMAttachments.ATTUNEMENTS).view();
    }

    /**
     * Evaluates whether {@code player} may pass a lock guarded by {@code guardingDefinitions},
     * posting {@link AttunementCheckEvent} first so other mods/scripts can force-allow or
     * force-deny. Default (unopinionated) behavior: allowed if the player holds any one of the
     * guarding definitions' attunement IDs.
     */
    public static boolean evaluate(ServerPlayer player, Identifier targetId,
                                    AttunementCheckEvent.LockType lockType,
                                    List<AttunementDefinition> guardingDefinitions) {
        AttunementCheckEvent check = new AttunementCheckEvent(player, targetId, lockType);
        NeoForge.EVENT_BUS.post(check);
        return switch (check.getResult()) {
            case ALLOW -> true;
            case DENY -> false;
            case DEFAULT -> guardingDefinitions.stream().anyMatch(def -> has(player, def.id()));
        };
    }
}
