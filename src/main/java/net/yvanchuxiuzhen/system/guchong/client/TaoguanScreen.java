package net.yvanchuxiuzhen.system.guchong.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanMenu;

/** 蛊罐原生界面沿用来源贴图、尺寸和槽位。 */
public final class TaoguanScreen extends AbstractContainerScreen<TaoguanMenu> {
    private final ResourceLocation texture;

    public TaoguanScreen(TaoguanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        texture = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/taoguan_0" + menu.grade() + "_gui.png");
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { }
}
