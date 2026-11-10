package net.yvanchuxiuzhen.system.cooking.client;

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
import net.yvanchuxiuzhen.system.cooking.content.WoodenStillMenu;
import net.yvanchuxiuzhen.system.cooking.network.WineButtonPayload;

/** 木甑使用来源五槽画布和逐层叠加的十档进度，仅将按钮请求发送至当前服务端菜单。 */
public final class WoodenStillScreen extends AbstractContainerScreen<WoodenStillMenu> {
    public WoodenStillScreen(WoodenStillMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addButton(87, 20, 18, 27, "pengrenbegin01", 0, "gui.yvanchuxiuzhen.cooking.wine.start");
        addButton(22, 27, 12, 12, "bankgui10", 1, "gui.yvanchuxiuzhen.cooking.wine.help");
    }

    private void addButton(int x, int y, int width, int height, String texture, int operation, String key) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + texture), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new WineButtonPayload(menu.containerId, operation)));
        button.setMessage(Component.translatable(key));
        addRenderableWidget(button);
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
        blit(graphics, "foodmuzeng_gui", 0, 0, imageWidth, imageHeight);
        blit(graphics, "muzeng", -9, -29, 194, 112);
        double progress = menu.progress();
        for (int stage = 1; stage <= 10; stage++) {
            if (progress >= 5 + (stage - 1) * 10) {
                blit(graphics, "muzengjindu" + stage, -7, -27, 190, 8);
            }
        }
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
