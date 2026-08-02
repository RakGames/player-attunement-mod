package com.zenil.pam.event;

import com.zenil.pam.PAM;
import com.zenil.pam.api.AttunementApi;
import com.zenil.pam.attunement.AttunementData;
import com.zenil.pam.attunement.AttunementDefinition;
import com.zenil.pam.attunement.PAMAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

/**
 * Records advancement/craft/kill history and re-evaluates every active-preset attunement whenever
 * a player's progress changes. A definition grants once ALL of its non-empty requirement
 * categories are satisfied: every listed advancement earned, at least one listed item crafted (if
 * any are listed), at least one listed entity killed (if any are listed), and player level at or
 * above the minimum (if set). A definition with no requirements at all is never auto-granted —
 * it's reachable only via {@code /pam grant} or the API.
 */
@EventBusSubscriber(modid = PAM.MOD_ID)
public final class TriggerHandler {
    private TriggerHandler() {}

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AttunementData data = player.getData(PAMAttachments.ATTUNEMENTS);
        data.recordAdvancementEarned(event.getAdvancement().id());
        reevaluateAll(player, data);
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AttunementData data = player.getData(PAMAttachments.ATTUNEMENTS);
        data.recordItemCrafted(BuiltInRegistries.ITEM.getKey(event.getCrafting().getItem()));
        reevaluateAll(player, data);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        AttunementData data = player.getData(PAMAttachments.ATTUNEMENTS);
        data.recordEntityKilled(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()));
        reevaluateAll(player, data);
    }

    @SubscribeEvent
    public static void onLevelChange(PlayerXpEvent.LevelChange event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        reevaluateAll(player, player.getData(PAMAttachments.ATTUNEMENTS));
    }

    private static void reevaluateAll(ServerPlayer player, AttunementData data) {
        for (AttunementDefinition def : PAM.ATTUNEMENT_MANAGER.all()) {
            if (data.has(def.id())) continue;
            if (requirementsMet(player, data, def)) {
                AttunementApi.grant(player, def.id());
            }
        }
    }

    private static boolean requirementsMet(ServerPlayer player, AttunementData data, AttunementDefinition def) {
        AttunementDefinition.Requirements req = def.requirements();
        boolean hasAnyRequirement = !req.advancements().isEmpty() || !req.itemsCrafted().isEmpty()
            || !req.entitiesKilled().isEmpty() || req.minPlayerLevel() > 0;
        if (!hasAnyRequirement) return false;

        if (!req.advancements().isEmpty() && !data.hasEarnedAll(req.advancements())) return false;
        if (!req.itemsCrafted().isEmpty() && !data.hasCraftedAny(req.itemsCrafted())) return false;
        if (!req.entitiesKilled().isEmpty() && !data.hasKilledAny(req.entitiesKilled())) return false;
        return req.minPlayerLevel() <= 0 || player.experienceLevel >= req.minPlayerLevel();
    }
}
