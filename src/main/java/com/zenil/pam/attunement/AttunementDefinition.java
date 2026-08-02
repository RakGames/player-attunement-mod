package com.zenil.pam.attunement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Preset-defined attunement: what unlocks it, and what it gates. Bundled inside a {@code PresetDefinition}. */
public record AttunementDefinition(Identifier id, Requirements requirements, Locks locks, String denialMessage) {

    private static final Requirements NO_REQUIREMENTS = new Requirements(List.of(), List.of(), List.of(), 0);
    private static final Locks NO_LOCKS = new Locks(List.of(), List.of(), List.of(), List.of());

    public static final Codec<AttunementDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.fieldOf("id").forGetter(AttunementDefinition::id),
        Requirements.CODEC.optionalFieldOf("requirements", NO_REQUIREMENTS).forGetter(AttunementDefinition::requirements),
        Locks.CODEC.optionalFieldOf("locks", NO_LOCKS).forGetter(AttunementDefinition::locks),
        Codec.STRING.optionalFieldOf("denial_message", "pam.denied.generic").forGetter(AttunementDefinition::denialMessage)
    ).apply(instance, AttunementDefinition::new));

    /**
     * What must be true to auto-grant this attunement, evaluated by {@code com.zenil.pam.event.TriggerHandler}.
     * Categories combine with AND: every non-empty category must be satisfied. Within a category,
     * {@code advancements} requires ALL listed advancements (e.g. "requires X and Y"); {@code itemsCrafted}
     * and {@code entitiesKilled} require ANY ONE listed entry (e.g. "craft at least one piece of...").
     * {@code minPlayerLevel} of 0 means no level requirement.
     */
    public record Requirements(List<Identifier> advancements, List<Identifier> itemsCrafted,
                                List<Identifier> entitiesKilled, int minPlayerLevel) {
        public static final Codec<Requirements> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.listOf().optionalFieldOf("advancements", List.of()).forGetter(Requirements::advancements),
            Identifier.CODEC.listOf().optionalFieldOf("items_crafted", List.of()).forGetter(Requirements::itemsCrafted),
            Identifier.CODEC.listOf().optionalFieldOf("entities_killed", List.of()).forGetter(Requirements::entitiesKilled),
            Codec.INT.optionalFieldOf("min_player_level", 0).forGetter(Requirements::minPlayerLevel)
        ).apply(instance, Requirements::new));
    }

    /**
     * Content gated behind this attunement. {@code items} covers armor equip, right-click use, and
     * mining/tool use (see {@code ItemLockHandler}/{@code BlockBreakLockHandler}), and totem-of-undying
     * activation (see {@code TotemLockHandler}). {@code blocks} covers right-click block interaction
     * (beacons, ender chests, shulker boxes; see {@code BlockInteractLockHandler}). Entity locks are
     * exposed for API consumers; PAM ships no built-in entity-interaction handler out of the box.
     */
    public record Locks(List<Identifier> dimensions, List<Identifier> items, List<Identifier> entities, List<Identifier> blocks) {
        public static final Codec<Locks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.listOf().optionalFieldOf("dimensions", List.of()).forGetter(Locks::dimensions),
            Identifier.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(Locks::items),
            Identifier.CODEC.listOf().optionalFieldOf("entities", List.of()).forGetter(Locks::entities),
            Identifier.CODEC.listOf().optionalFieldOf("blocks", List.of()).forGetter(Locks::blocks)
        ).apply(instance, Locks::new));
    }
}
