package com.zenil.pam.attunement;

import com.zenil.pam.PAM;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class PAMAttachments {
    private PAMAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PAM.MOD_ID);

    public static final Supplier<AttachmentType<AttunementData>> ATTUNEMENTS = ATTACHMENT_TYPES.register(
        "attunements",
        () -> AttachmentType.builder(AttunementData::new)
            .serialize(AttunementData.CODEC.fieldOf("data"))
            .copyOnDeath()
            .build()
    );
}
