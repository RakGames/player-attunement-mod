package com.zenil.pam.preset;

import com.zenil.pam.PAM;
import com.zenil.pam.attunement.AttunementDefinition;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads {@code data/<namespace>/presets/*.json} on every datapack reload, and separately tracks
 * which preset is currently active for this world. Reloading a datapack refreshes the active
 * preset's rule set (in case its file changed) without changing WHICH preset is active — that's
 * per-world persisted state, owned by {@code PAMWorldState}.
 */
public final class PresetManager extends SimpleJsonResourceReloadListener<PresetDefinition> {

    private static final String DIRECTORY = "presets";

    private Map<Identifier, PresetDefinition> presets = Map.of();
    private Identifier activePresetId = PAMPreset.NONE.id();

    public PresetManager() {
        super(PresetDefinition.CODEC, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(Map<Identifier, PresetDefinition> loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, PresetDefinition> byId = new HashMap<>();
        for (PresetDefinition def : loaded.values()) {
            byId.put(def.id(), def);
        }
        this.presets = Map.copyOf(byId);
        PAM.LOGGER.info("PAM loaded {} preset(s): {}", presets.size(), presets.keySet());
        applyActiveToIndex();
    }

    public Collection<PresetDefinition> all() {
        return presets.values();
    }

    public Optional<PresetDefinition> get(Identifier id) {
        return Optional.ofNullable(presets.get(id));
    }

    public Identifier getActivePresetId() {
        return activePresetId;
    }

    /** Switches the active preset in memory and rebuilds the attunement index. Does not persist to disk. */
    public void setActivePreset(Identifier id) {
        this.activePresetId = id;
        applyActiveToIndex();
    }

    /** Switches the active preset and persists the choice on the overworld, surviving restarts. */
    public void activateAndPersist(MinecraftServer server, Identifier id) {
        setActivePreset(id);
        server.overworld().getData(PAMWorldAttachments.WORLD_STATE).setActivePreset(id);
    }

    private void applyActiveToIndex() {
        List<AttunementDefinition> attunements = presets.containsKey(activePresetId)
            ? presets.get(activePresetId).attunements()
            : List.of();
        PAM.ATTUNEMENT_MANAGER.rebuild(attunements);
    }
}
