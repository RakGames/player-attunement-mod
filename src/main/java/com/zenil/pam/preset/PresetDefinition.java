package com.zenil.pam.preset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zenil.pam.attunement.AttunementDefinition;
import net.minecraft.resources.Identifier;

import java.util.List;

/** A named bundle of attunement definitions, loaded from {@code data/pam/presets/*.json}. */
public record PresetDefinition(Identifier id, String name, List<AttunementDefinition> attunements) {
    public static final Codec<PresetDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.fieldOf("id").forGetter(PresetDefinition::id),
        Codec.STRING.fieldOf("name").forGetter(PresetDefinition::name),
        AttunementDefinition.CODEC.listOf().optionalFieldOf("attunements", List.of()).forGetter(PresetDefinition::attunements)
    ).apply(instance, PresetDefinition::new));
}
