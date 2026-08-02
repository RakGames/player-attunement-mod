package com.zenil.pam.preset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * Per-world persisted record of which preset was chosen for this world, and whether the first-join
 * preset picker has already been shown. Attached to the overworld {@code Level}. A {@code null}
 * active preset means "not yet resolved" (fresh world, or a world that predates PAM). Once
 * resolved, the choice is sticky for the life of the world — datapack reloads refresh the active
 * preset's rules but never change which preset that is.
 */
public final class PAMWorldState {

    public static final Codec<PAMWorldState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.optionalFieldOf("active_preset").forGetter(s -> Optional.ofNullable(s.activePreset)),
        Codec.BOOL.optionalFieldOf("shown_first_join_prompt", false).forGetter(s -> s.shownFirstJoinPrompt)
    ).apply(instance, (activePreset, shown) -> new PAMWorldState(activePreset.orElse(null), shown)));

    private Identifier activePreset;
    private boolean shownFirstJoinPrompt;

    public PAMWorldState() {
        this(null, false);
    }

    private PAMWorldState(Identifier activePreset, boolean shownFirstJoinPrompt) {
        this.activePreset = activePreset;
        this.shownFirstJoinPrompt = shownFirstJoinPrompt;
    }

    public boolean isResolved() {
        return activePreset != null;
    }

    public Identifier getActivePreset() {
        return activePreset;
    }

    public void setActivePreset(Identifier id) {
        this.activePreset = id;
    }

    public boolean hasShownFirstJoinPrompt() {
        return shownFirstJoinPrompt;
    }

    public void markFirstJoinPromptShown() {
        this.shownFirstJoinPrompt = true;
    }
}
