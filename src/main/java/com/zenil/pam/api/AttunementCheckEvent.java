package com.zenil.pam.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Fired whenever PAM evaluates whether a player may pass a lock (dimension travel, item
 * equip/use). Third-party mods, KubeJS, or CraftTweaker scripts can set a result to force-allow
 * or force-deny regardless of the player's stored attunements. Leaving the result at DEFAULT
 * falls back to PAM's own attunement check.
 */
public class AttunementCheckEvent extends Event {

    public enum LockType { DIMENSION, ITEM, ENTITY, BLOCK }

    public enum Result { DEFAULT, ALLOW, DENY }

    private final ServerPlayer player;
    private final Identifier targetId;
    private final LockType lockType;
    private Result result = Result.DEFAULT;

    public AttunementCheckEvent(ServerPlayer player, Identifier targetId, LockType lockType) {
        this.player = player;
        this.targetId = targetId;
        this.lockType = lockType;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Identifier getTargetId() {
        return targetId;
    }

    public LockType getLockType() {
        return lockType;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
