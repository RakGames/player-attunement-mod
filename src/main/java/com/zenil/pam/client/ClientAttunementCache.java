package com.zenil.pam.client;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

/** Client-side mirror of the local player's unlocked attunements, kept in sync via {@code SyncAttunementsPayload}. */
public final class ClientAttunementCache {
    private static Set<Identifier> unlocked = Set.of();

    private ClientAttunementCache() {}

    public static void set(List<Identifier> ids) {
        unlocked = Set.copyOf(ids);
    }

    public static boolean has(Identifier id) {
        return unlocked.contains(id);
    }
}
