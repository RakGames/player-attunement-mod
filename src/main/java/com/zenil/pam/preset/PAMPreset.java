package com.zenil.pam.preset;

import com.zenil.pam.PAM;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * The three built-in progression difficulty presets. {@link #NONE} has no backing datapack file —
 * it simply resolves to zero active attunement definitions, so every lock check passes.
 * {@link #VANILLA_PLUS} and {@link #ATTUNEMENT_PROGRESSION} are defined by
 * {@code data/pam/presets/vanilla_plus.json} / {@code attunement_progression.json}.
 */
public enum PAMPreset {
    NONE("none", "sandbox", "pam.preset.none.name", "pam.preset.none.description"),
    VANILLA_PLUS("vanilla_plus", "vanilla_plus", "pam.preset.vanilla_plus.name", "pam.preset.vanilla_plus.description"),
    ATTUNEMENT_PROGRESSION("attunement_progression", "rpg_progression", "pam.preset.attunement_progression.name", "pam.preset.attunement_progression.description");

    /** Real pixel dimensions of the splash art source files; display size is computed per-screen from these. */
    public static final int SPLASH_ART_SOURCE_WIDTH = 448;
    public static final int SPLASH_ART_SOURCE_HEIGHT = 600;

    private final Identifier id;
    private final Identifier splashArt;
    private final String nameKey;
    private final String descriptionKey;

    PAMPreset(String path, String splashArtName, String nameKey, String descriptionKey) {
        this.id = Identifier.fromNamespaceAndPath(PAM.MOD_ID, path);
        this.splashArt = Identifier.fromNamespaceAndPath(PAM.MOD_ID, "textures/gui/pam_splash_" + splashArtName + ".png");
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
    }

    public Identifier id() {
        return id;
    }

    public String configName() {
        return id.getPath();
    }

    public Identifier splashArt() {
        return splashArt;
    }

    public Component displayName() {
        return Component.translatable(nameKey);
    }

    public Component description() {
        return Component.translatable(descriptionKey);
    }

    public static Optional<PAMPreset> byConfigName(String name) {
        for (PAMPreset preset : values()) {
            if (preset.configName().equals(name)) {
                return Optional.of(preset);
            }
        }
        return Optional.empty();
    }

    public static Optional<PAMPreset> byId(Identifier id) {
        for (PAMPreset preset : values()) {
            if (preset.id.equals(id)) {
                return Optional.of(preset);
            }
        }
        return Optional.empty();
    }
}
