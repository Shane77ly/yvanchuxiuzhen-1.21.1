package net.yvanchuxiuzhen.collection.decor.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.collection.decor.DisplayFurnitureMenu;
import net.yvanchuxiuzhen.platform.ModId;

/** 展示家具保留来源背景、槽位和博古架切换按钮，界面不绘制标签。 */
public final class DisplayFurnitureScreen extends AbstractContainerScreen<DisplayFurnitureMenu> {
    private final ResourceLocation texture;

    public DisplayFurnitureScreen(DisplayFurnitureMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        texture = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + (menu.cabinet() ? "beechfurniture_03_gui" : "beechfurniture_15_gui") + ".png");
    }

    @Override
    protected void init() {
        super.init();
        if (!menu.cabinet()) addRenderableWidget(Button.builder(
                Component.translatable("gui.yvanchuxiuzhen.beechfurniture_15_gui.button_qie_huan"),
                button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0))
                .bounds(leftPos + 69, topPos + 52, 35, 20).build());
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
