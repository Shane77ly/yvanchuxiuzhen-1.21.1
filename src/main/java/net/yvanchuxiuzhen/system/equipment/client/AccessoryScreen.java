package net.yvanchuxiuzhen.system.equipment.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.equipment.content.AccessoryMenu;

/** 来源 PlayerGUI03 的四槽与原生背包布局，操作由当前服务端菜单处理。 */
public final class AccessoryScreen extends AbstractContainerScreen<AccessoryMenu> {
    private static final List<String> NAVIGATION = List.of("属性显示", "属性加点", "饰品装备", "功法与技能", "副职业", "修炼");

    public AccessoryScreen(AccessoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        addButton(69, 147, 66, 18, "guipage01", 0, "属性显示");
        addButton(69, 228, 66, 18, "guipage013", 2, "功法与技能");
        addButton(366, 156, 12, 12, "bankgui10", 6, "饰品说明");
    }

    private void addButton(int x, int y, int width, int height, String name, int id, String message) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height, texture("atlas/imagebutton_" + name), width, height * 2,
                pressed -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id));
        button.setMessage(Component.literal(message));
        addRenderableWidget(button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        blit(graphics, "wupilanbiankuang", 162, 243, 176, 89);
        blit(graphics, "gui", 141, 136, 216, 130);
        blit(graphics, "gui03", 168, 162, 18, 78);
        blit(graphics, "guipage03", 69, 201, 66, 18);
        for (int y : new int[]{250, 268, 286, 308}) blit(graphics, "shipintaigui05", 169, y, 162, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int index = 0; index < NAVIGATION.size(); index++) {
            graphics.drawString(font, NAVIGATION.get(index), 72, 152 + index * 27, index == 2 ? -6710887 : -1, false);
        }
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
