package com.zenil.pam.client;

import com.zenil.pam.network.SelectPresetPayload;
import com.zenil.pam.preset.PAMPreset;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Shown once per world, the first time a player joins after PAM auto-resolves a default preset
 * (see {@code PAM#onPlayerLoggedIn}). Three side-by-side splash-art boxes, one per preset; picking
 * one sends {@link SelectPresetPayload} to override the default. Dismissing without choosing (Esc)
 * just keeps whatever preset was already auto-resolved — this screen never blocks play.
 *
 * <p>Box size is computed from the screen's own GUI-space dimensions (not a fixed pixel size), so
 * the art fills a consistent proportion of the window regardless of resolution or GUI Scale.
 */
public final class PAMFirstJoinScreen extends Screen {
    private static final int BOX_COUNT = PAMPreset.values().length;
    private static final int GAP = 16;
    private static final int TEXT_AREA_HEIGHT = 60;
    private static final int TOP_MARGIN = 48;
    private static final int SIDE_MARGIN_FRACTION_DIVISOR = 10; // reserve 1/10 of width as side margin

    private final PAMPreset current;

    public PAMFirstJoinScreen(PAMPreset current) {
        super(Component.translatable("pam.screen.firstjoin.title"));
        this.current = current;
    }

    @Override
    protected void init() {
        int sideMargin = this.width / SIDE_MARGIN_FRACTION_DIVISOR;
        int availableWidth = this.width - sideMargin * 2 - GAP * (BOX_COUNT - 1);
        int availableHeight = this.height - TOP_MARGIN - TEXT_AREA_HEIGHT - 24;

        int widthPerBox = availableWidth / BOX_COUNT;
        int heightFromWidth = widthPerBox * PAMPreset.SPLASH_ART_SOURCE_HEIGHT / PAMPreset.SPLASH_ART_SOURCE_WIDTH;

        int boxWidth;
        int boxHeight;
        if (heightFromWidth <= availableHeight) {
            boxWidth = widthPerBox;
            boxHeight = heightFromWidth;
        } else {
            boxHeight = availableHeight;
            boxWidth = availableHeight * PAMPreset.SPLASH_ART_SOURCE_WIDTH / PAMPreset.SPLASH_ART_SOURCE_HEIGHT;
        }

        int totalWidth = BOX_COUNT * boxWidth + (BOX_COUNT - 1) * GAP;
        int startX = this.width / 2 - totalWidth / 2;
        int boxY = TOP_MARGIN;

        this.addRenderableWidget(new StringWidget(this.width / 2 - 150, boxY - 24, 300, 20, this.title, this.font));

        int x = startX;
        for (PAMPreset preset : PAMPreset.values()) {
            int boxX = x;

            this.addRenderableWidget(new SplashArtButton(
                boxX, boxY, boxWidth, boxHeight, Component.empty(), preset.splashArt(),
                PAMPreset.SPLASH_ART_SOURCE_WIDTH, PAMPreset.SPLASH_ART_SOURCE_HEIGHT,
                b -> choose(preset)));

            Component nameLabel = preset == current
                ? Component.literal("> ").append(preset.displayName())
                : preset.displayName();
            this.addRenderableWidget(new StringWidget(boxX, boxY + boxHeight + 4, boxWidth, 12, nameLabel, this.font));

            MultiLineTextWidget description = new MultiLineTextWidget(boxX, boxY + boxHeight + 18, preset.description(), this.font);
            description.setMaxWidth(boxWidth);
            this.addRenderableWidget(description);

            x += boxWidth + GAP;
        }
    }

    private void choose(PAMPreset preset) {
        ClientPacketDistributor.sendToServer(new SelectPresetPayload(preset.id()));
        this.onClose();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(null);
    }
}
