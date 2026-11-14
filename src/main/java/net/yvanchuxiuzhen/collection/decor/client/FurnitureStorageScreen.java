package net.yvanchuxiuzhen.collection.decor.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.collection.decor.FurnitureStorageMenu;
import net.yvanchuxiuzhen.platform.ModId;

/** 储物家具使用三张来源背景及原槽位布局，名称只供界面叙述读取。 */
public final class FurnitureStorageScreen extends AbstractContainerScreen<FurnitureStorageMenu> {
    private final ResourceLocation texture;

    public FurnitureStorageScreen(FurnitureStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = menu.layout().height();
        texture = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + menu.layout().menuId() + ".png");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
