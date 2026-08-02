package com.zenil.pam.attunement;

import com.zenil.pam.PAM;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory index of the attunement definitions bundled in the currently active preset. Rebuilt
 * by {@code com.zenil.pam.preset.PresetManager} whenever the datapack reloads or the active
 * preset changes; the lock/trigger handlers read from this index and never touch presets directly.
 */
public final class AttunementManager {

    private Map<Identifier, AttunementDefinition> definitions = Map.of();
    private final Map<Identifier, List<AttunementDefinition>> byDimensionLock = new HashMap<>();
    private final Map<Identifier, List<AttunementDefinition>> byItemLock = new HashMap<>();
    private final Map<Identifier, List<AttunementDefinition>> byEntityLock = new HashMap<>();
    private final Map<Identifier, List<AttunementDefinition>> byBlockLock = new HashMap<>();

    /** Replaces the active index. Called with an empty collection for the NONE preset. */
    public void rebuild(Collection<AttunementDefinition> active) {
        Map<Identifier, AttunementDefinition> byId = new HashMap<>();
        for (AttunementDefinition def : active) {
            byId.put(def.id(), def);
        }
        this.definitions = Map.copyOf(byId);

        byDimensionLock.clear();
        byItemLock.clear();
        byEntityLock.clear();
        byBlockLock.clear();
        for (AttunementDefinition def : definitions.values()) {
            def.locks().dimensions().forEach(lockId -> byDimensionLock.computeIfAbsent(lockId, k -> new ArrayList<>()).add(def));
            def.locks().items().forEach(lockId -> byItemLock.computeIfAbsent(lockId, k -> new ArrayList<>()).add(def));
            def.locks().entities().forEach(lockId -> byEntityLock.computeIfAbsent(lockId, k -> new ArrayList<>()).add(def));
            def.locks().blocks().forEach(lockId -> byBlockLock.computeIfAbsent(lockId, k -> new ArrayList<>()).add(def));
        }

        PAM.LOGGER.info("PAM active preset has {} attunement definition(s)", definitions.size());
    }

    public Collection<AttunementDefinition> all() {
        return definitions.values();
    }

    public Optional<AttunementDefinition> get(Identifier id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public List<AttunementDefinition> lockingDimension(Identifier dimensionId) {
        return byDimensionLock.getOrDefault(dimensionId, List.of());
    }

    public List<AttunementDefinition> lockingItem(Identifier itemId) {
        return byItemLock.getOrDefault(itemId, List.of());
    }

    public List<AttunementDefinition> lockingEntity(Identifier entityId) {
        return byEntityLock.getOrDefault(entityId, List.of());
    }

    public List<AttunementDefinition> lockingBlock(Identifier blockId) {
        return byBlockLock.getOrDefault(blockId, List.of());
    }
}
