package net.yvanchuxiuzhen.system.economy.client;

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
import net.yvanchuxiuzhen.system.economy.content.BankLoginMenu;
import net.yvanchuxiuzhen.system.economy.network.BankButtonPayload;

/** 来源通宝玉登录页；按钮向当前服务端容器发操作，不传卡数据或账户。 */
public final class BankLoginScreen extends AbstractContainerScreen<BankLoginMenu> {
    public BankLoginScreen(BankLoginMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        addButton(155, 155, 51, 20, "bankgui07", 0, "enter");
        addButton(357, 156, 12, 12, "bankgui12", 1, "close");
        addButton(339, 156, 12, 12, "bankgui10", 2, "explain");
    }

    private void addButton(int x, int y, int width, int height, String image, int operation, String label) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + image), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new BankButtonPayload(menu.containerId, operation)));
        button.setMessage(text(label));
        addRenderableWidget(button);
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
        drawTexture(graphics, "wupinlan", 162, 255, 176, 94);
        drawTexture(graphics, "bankgui01", 111, 113, 276, 151);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, text("title"), 233, 131, -154, false);
    }

    private void drawTexture(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static Component text(String suffix) {
        return Component.translatable("gui.yvanchuxiuzhen.bank_gu_idenglu." + suffix);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
