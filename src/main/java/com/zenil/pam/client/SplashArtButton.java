package com.zenil.pam.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A button that renders a full splash-art texture in place of the vanilla button sprite, via a
 * direct (non-atlas) blit — {@code textureWidth}/{@code textureHeight} are the source PNG's real
 * dimensions, used only for UV normalization; the button's own width/height control display size.
 */
final class SplashArtButton extends Button {
    private final Identifier texture;
    private final int textureWidth;
    private final int textureHeight;

    SplashArtButton(int x, int y, int width, int height, Component message, Identifier texture,
                     int textureWidth, int textureHeight, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // srcWidth/srcHeight = the full texture, distinct from width/height (the display size) —
        // this is what actually scales the source image down, rather than cropping a corner of it.
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), 0.0F, 0.0F,
            width, height, textureWidth, textureHeight, textureWidth, textureHeight);
    }
}
