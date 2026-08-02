package com.zenil.pam;

import com.mojang.logging.LogUtils;
import com.zenil.pam.attunement.AttunementManager;
import com.zenil.pam.attunement.PAMAttachments;
import com.zenil.pam.command.PAMCommand;
import com.zenil.pam.config.PAMServerConfig;
import com.zenil.pam.network.PAMNetworking;
import com.zenil.pam.preset.PAMWorldAttachments;
import com.zenil.pam.preset.PAMWorldState;
import com.zenil.pam.preset.PresetManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

/**
 * DimensionLockHandler, ItemLockHandler, TriggerHandler, BlockBreakLockHandler,
 * BlockInteractLockHandler, and TotemLockHandler in {@code com.zenil.pam.event} self-register via
 * {@code @EventBusSubscriber(modid = MOD_ID)} and need no wiring here.
 */
@Mod(PAM.MOD_ID)
public final class PAM {
    public static final String MOD_ID = "pam";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final AttunementManager ATTUNEMENT_MANAGER = new AttunementManager();
    public static final PresetManager PRESET_MANAGER = new PresetManager();

    public PAM(IEventBus modEventBus, ModContainer modContainer) {
        PAMAttachments.ATTACHMENT_TYPES.register(modEventBus);
        PAMWorldAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(PAMNetworking::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, PAMServerConfig.SPEC, "pam-server.toml");

        NeoForge.EVENT_BUS.addListener(PAMCommand::register);
        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) ->
            event.addListener(Identifier.fromNamespaceAndPath(MOD_ID, "presets"), PRESET_MANAGER));
        NeoForge.EVENT_BUS.addListener(PAM::onServerStarting);
        NeoForge.EVENT_BUS.addListener(PAM::onPlayerLoggedIn);
    }

    /**
     * Resolves which preset this world runs. A world that's already picked one (via
     * {@code PAMWorldState}, written by {@code /pam preset set} or a prior player choice) keeps
     * it forever — presets are sticky per world. A fresh world always resolves immediately to the
     * dedicated-server config default, so the world is never left in a half-configured state; in
     * singleplayer, {@link #onPlayerLoggedIn} then offers the joining player a one-time chance to
     * override that default via {@code PAMFirstJoinScreen}.
     */
    private static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        PAMWorldState state = server.getLevel(Level.OVERWORLD).getData(PAMWorldAttachments.WORLD_STATE);

        if (!state.isResolved()) {
            state.setActivePreset(PAMServerConfig.resolveDefaultPreset().id());
        }

        PRESET_MANAGER.setActivePreset(state.getActivePreset());
        LOGGER.info("PAM active progression preset: {}", state.getActivePreset());
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PAMNetworking.syncTo(player);

        MinecraftServer server = player.level().getServer();
        if (server.isDedicatedServer()) return;

        PAMWorldState state = server.getLevel(Level.OVERWORLD).getData(PAMWorldAttachments.WORLD_STATE);
        if (!state.hasShownFirstJoinPrompt()) {
            state.markFirstJoinPromptShown();
            PAMNetworking.sendShowPresetPicker(player, state.getActivePreset());
        }
    }
}
