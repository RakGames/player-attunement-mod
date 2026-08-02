package com.zenil.pam.preset;

import com.zenil.pam.PAM;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class PAMWorldAttachments {
    private PAMWorldAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PAM.MOD_ID);

    public static final Supplier<AttachmentType<PAMWorldState>> WORLD_STATE = ATTACHMENT_TYPES.register(
        "world_state",
        () -> AttachmentType.builder(PAMWorldState::new)
            .serialize(PAMWorldState.CODEC.fieldOf("data"))
            .build()
    );
}
