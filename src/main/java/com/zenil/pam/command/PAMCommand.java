package com.zenil.pam.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.zenil.pam.PAM;
import com.zenil.pam.api.AttunementApi;
import com.zenil.pam.attunement.AttunementDefinition;
import com.zenil.pam.preset.PAMPreset;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/** {@code /pam grant|revoke|list|clear} — gated behind {@code Commands.LEVEL_GAMEMASTERS} (old op-level 2 equivalent). */
public final class PAMCommand {
    private PAMCommand() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("pam")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("grant")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("attunement_id", IdentifierArgument.id())
                            .suggests(PAMCommand::suggestAttunements)
                            .executes(PAMCommand::grant))))
                .then(Commands.literal("revoke")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("attunement_id", IdentifierArgument.id())
                            .suggests(PAMCommand::suggestAttunements)
                            .executes(PAMCommand::revoke))))
                .then(Commands.literal("list")
                    .executes(ctx -> list(ctx, ctx.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> list(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("clear")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(PAMCommand::clear)))
                .then(Commands.literal("preset")
                    .then(Commands.literal("get").executes(PAMCommand::presetGet))
                    .then(Commands.literal("set")
                        .then(Commands.literal("none").executes(ctx -> presetSet(ctx, PAMPreset.NONE)))
                        .then(Commands.literal("vanilla_plus").executes(ctx -> presetSet(ctx, PAMPreset.VANILLA_PLUS)))
                        .then(Commands.literal("attunement_progression").executes(ctx -> presetSet(ctx, PAMPreset.ATTUNEMENT_PROGRESSION)))))
        );
    }

    private static int grant(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        Identifier id = IdentifierArgument.getId(ctx, "attunement_id");
        boolean changed = AttunementApi.grant(player, id);
        if (changed) {
            ctx.getSource().sendSuccess(() -> Component.translatable("pam.command.grant.success", id.toString(), player.getDisplayName()), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("pam.command.grant.already", id.toString(), player.getDisplayName()));
        return 0;
    }

    private static int revoke(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        Identifier id = IdentifierArgument.getId(ctx, "attunement_id");
        boolean changed = AttunementApi.revoke(player, id);
        if (changed) {
            ctx.getSource().sendSuccess(() -> Component.translatable("pam.command.revoke.success", id.toString(), player.getDisplayName()), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("pam.command.revoke.missing", id.toString(), player.getDisplayName()));
        return 0;
    }

    private static int list(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Set<Identifier> unlocked = AttunementApi.unlocked(player);
        if (unlocked.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("pam.command.list.empty", player.getDisplayName()), false);
            return 0;
        }
        String joined = unlocked.stream().map(Identifier::toString).sorted().collect(Collectors.joining(", "));
        ctx.getSource().sendSuccess(() -> Component.translatable("pam.command.list.header", player.getDisplayName(), unlocked.size())
            .append(Component.literal(": " + joined)), false);
        return unlocked.size();
    }

    private static int clear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        AttunementApi.clear(player);
        ctx.getSource().sendSuccess(() -> Component.translatable("pam.command.clear.success", player.getDisplayName()), true);
        return 1;
    }

    private static int presetGet(CommandContext<CommandSourceStack> ctx) {
        PAMPreset current = PAMPreset.byId(PAM.PRESET_MANAGER.getActivePresetId()).orElse(PAMPreset.NONE);
        ctx.getSource().sendSuccess(() -> Component.translatable("pam.command.preset.get", current.displayName()), false);
        return 1;
    }

    private static int presetSet(CommandContext<CommandSourceStack> ctx, PAMPreset preset) {
        PAM.PRESET_MANAGER.activateAndPersist(ctx.getSource().getServer(), preset.id());
        ctx.getSource().sendSuccess(() -> Component.translatable("pam.command.preset.set", preset.displayName()), true);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestAttunements(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(
            PAM.ATTUNEMENT_MANAGER.all().stream().map(AttunementDefinition::id), builder);
    }
}
