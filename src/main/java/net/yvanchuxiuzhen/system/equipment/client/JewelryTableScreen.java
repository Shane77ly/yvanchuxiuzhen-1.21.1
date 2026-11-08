package net.yvanchuxiuzhen.system.equipment.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.equipment.content.JewelryTableMenu;
import net.yvanchuxiuzhen.system.equipment.network.JewelryTableButtonPayload;

/** 饰品制作台的来源贴图与控件布局；类别由菜单读取，按钮交由服务端当前菜单处理。 */
public final class JewelryTableScreen extends AbstractContainerScreen<JewelryTableMenu> {
    public JewelryTableScreen(JewelryTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addButton(96, 65, 18, 18, "shipintaiguianniu01", 0, "合成");
        addButton(60, 52, 8, 10, "gui0401down01", 1, "下一类");
        addButton(106, 52, 8, 10, "gui0401up01", 2, "上一类");
        addButton(-21, 75, 18, 18, "shipintaiguitishi01", 3, "头簪配方提示");
        addButton(-21, 95, 18, 18, "shipintaiguitishi03", 4, "项坠配方提示");
        addButton(-21, 115, 18, 18, "shipintaiguitishi05", 5, "手镯配方提示");
        addButton(-21, 135, 18, 18, "shipintaiguitishi07", 6, "腰佩配方提示");
    }

    private void addButton(int x, int y, int width, int height, String name, int id, String message) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + name), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new JewelryTableButtonPayload(id)));
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
        blit(graphics, "shipintaigui", 0, 0, 176, 166);
        for (int y : new int[] {141, 119, 101, 83}) {
            blit(graphics, "shipintaigui05", 6, y, 162, 18);
        }
        blit(graphics, "shipintaigui01", 42, 65, 18, 18);
        blit(graphics, "shipintaigui02", 60, 65, 18, 18);
        blit(graphics, "shipintaigui03", 78, 65, 18, 18);
        blit(graphics, "shipintaigui04", 114, 65, 18, 18);
        blit(graphics, "shipintaigui06", 69, 52, 36, 10);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, menu.category(), 79, 53, -1, false);
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
