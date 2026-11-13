package net.yvanchuxiuzhen.system.book.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.book.BookReadingText;
import net.yvanchuxiuzhen.system.book.content.BookReadingMenu;

/** 阅读页沿用来源两层贴图和十一行坐标，只读当前主手与所属系统同步的实例。 */
public final class BookReadingScreen extends AbstractContainerScreen<BookReadingMenu> {
    private static final ResourceLocation BACKGROUND = texture("book_gui");
    private static final ResourceLocation SHEET = texture("bookgui");

    public BookReadingScreen(BookReadingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        graphics.blit(SHEET, leftPos - 42, topPos - 24, 0, 0, 260, 213, 260, 213);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        List<String> lines = BookReadingText.lines(minecraft.player);
        for (int index = 0; index < lines.size(); index++) {
            graphics.drawString(font, lines.get(index), -11, -11 + index * 18, -12829636, false);
        }
    }
}
