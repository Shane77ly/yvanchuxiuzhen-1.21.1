package net.yvanchuxiuzhen.platform.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 用整张贴图绘制的按钮，对应 1.18.2 版本 MCreator 生成的 {@code ImageButton}：贴图上半为常态，
 * 鼠标悬停或获得焦点时取下半（纵向偏移等于按钮高度）。1.21.1 的 {@code ImageButton} 改用精灵图集，不再接受这种贴图。
 */
public final class TextureButton extends Button {
    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;

    public TextureButton(int x, int y, int width, int height, ResourceLocation texture, int textureWidth, int textureHeight, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(texture, getX(), getY(), 0, isHoveredOrFocused() ? height : 0, width, height, textureWidth, textureHeight);
    }
}
