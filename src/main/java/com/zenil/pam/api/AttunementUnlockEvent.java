package com.zenil.pam.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Fired after an attunement is newly granted to a player. Not fired on redundant grants. */
public class AttunementUnlockEvent extends Event {

    private final ServerPlayer player;
    private final Identifier attunementId;

    public AttunementUnlockEvent(ServerPlayer player, Identifier attunementId) {
        this.player = player;
        this.attunementId = attunementId;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Identifier getAttunementId() {
        return attunementId;
    }
}
