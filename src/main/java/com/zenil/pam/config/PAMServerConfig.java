package com.zenil.pam.config;

import com.zenil.pam.preset.PAMPreset;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Global {@code config/pam-server.toml}, auto-generated on first startup by FML's config system.
 * Dedicated servers have no client UI prompt, so this is the sole source of truth for which
 * preset a world with no per-world choice yet should start on (see {@code PAMWorldState}).
 */
public final class PAMServerConfig {
    private PAMServerConfig() {}

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_PRESET;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("general");
        DEFAULT_PRESET = builder
            .comment(
                "The progression preset applied to any world on this server that has not chosen one yet.",
                "One of: none, vanilla_plus, attunement_progression"
            )
            .define("default_preset", PAMPreset.VANILLA_PLUS.configName(), PAMServerConfig::isValidPresetName);
        builder.pop();
        SPEC = builder.build();
    }

    public static PAMPreset resolveDefaultPreset() {
        return PAMPreset.byConfigName(DEFAULT_PRESET.get()).orElse(PAMPreset.VANILLA_PLUS);
    }

    private static boolean isValidPresetName(Object value) {
        return value instanceof String name && PAMPreset.byConfigName(name).isPresent();
    }
}
