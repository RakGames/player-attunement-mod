package com.zenil.pam.attunement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Mutable, server-authoritative per-player progression state: unlocked attunement IDs, plus a
 * running history of advancements earned, item types crafted, and entity types killed. The
 * history is tracked here (rather than queried from vanilla state) so all three requirement
 * categories evaluate the same way — see {@code TriggerHandler}, which requires ALL listed
 * advancements but ANY ONE listed crafted item / killed entity.
 */
public final class AttunementData {

    public static final Codec<AttunementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.listOf().optionalFieldOf("unlocked", List.of()).forGetter(d -> List.copyOf(d.unlocked)),
        Identifier.CODEC.listOf().optionalFieldOf("earned_advancements", List.of()).forGetter(d -> List.copyOf(d.earnedAdvancements)),
        Identifier.CODEC.listOf().optionalFieldOf("crafted_items", List.of()).forGetter(d -> List.copyOf(d.craftedItems)),
        Identifier.CODEC.listOf().optionalFieldOf("killed_entities", List.of()).forGetter(d -> List.copyOf(d.killedEntities))
    ).apply(instance, AttunementData::new));

    private final Set<Identifier> unlocked;
    private final Set<Identifier> earnedAdvancements;
    private final Set<Identifier> craftedItems;
    private final Set<Identifier> killedEntities;

    public AttunementData() {
        this(List.of(), List.of(), List.of(), List.of());
    }

    private AttunementData(List<Identifier> unlocked, List<Identifier> earnedAdvancements,
                            List<Identifier> craftedItems, List<Identifier> killedEntities) {
        this.unlocked = new LinkedHashSet<>(unlocked);
        this.earnedAdvancements = new LinkedHashSet<>(earnedAdvancements);
        this.craftedItems = new LinkedHashSet<>(craftedItems);
        this.killedEntities = new LinkedHashSet<>(killedEntities);
    }

    public boolean has(Identifier id) {
        return unlocked.contains(id);
    }

    public boolean grant(Identifier id) {
        return unlocked.add(id);
    }

    public boolean revoke(Identifier id) {
        return unlocked.remove(id);
    }

    public void clear() {
        unlocked.clear();
    }

    public Set<Identifier> view() {
        return Collections.unmodifiableSet(unlocked);
    }

    public boolean recordAdvancementEarned(Identifier advancementId) {
        return earnedAdvancements.add(advancementId);
    }

    public boolean recordItemCrafted(Identifier itemId) {
        return craftedItems.add(itemId);
    }

    public boolean recordEntityKilled(Identifier entityId) {
        return killedEntities.add(entityId);
    }

    public boolean hasEarnedAll(List<Identifier> advancementIds) {
        return earnedAdvancements.containsAll(advancementIds);
    }

    public boolean hasCraftedAny(List<Identifier> itemIds) {
        return itemIds.stream().anyMatch(craftedItems::contains);
    }

    public boolean hasKilledAny(List<Identifier> entityIds) {
        return entityIds.stream().anyMatch(killedEntities::contains);
    }
}
