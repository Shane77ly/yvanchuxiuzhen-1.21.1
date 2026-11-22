package net.yvanchuxiuzhen.system.book.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.book.content.BookBindingMenu;

/** 来源装订台使用176×204背景和纸、线图示，无文字标签与操作按钮。 */
public final class BookBindingScreen extends AbstractContainerScreen<BookBindingMenu> {
    public BookBindingScreen(BookBindingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture("makebook_gui"), leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        graphics.blit(texture("paper"), leftPos + 133, topPos + 99, 0, 0, 16, 16, 16, 16);
        graphics.blit(texture("string"), leftPos + 151, topPos + 99, 0, 0, 16, 16, 16, 16);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
