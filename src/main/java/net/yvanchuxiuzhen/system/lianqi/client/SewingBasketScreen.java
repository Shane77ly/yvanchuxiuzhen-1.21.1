package net.yvanchuxiuzhen.system.lianqi.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.content.SewingBasketMenu;
import net.yvanchuxiuzhen.system.lianqi.network.SewingBasketPayload;

/** 缝纫原生界面沿用来源画布、槽位和三按钮布局，客户端只发送操作。 */
public final class SewingBasketScreen extends AbstractContainerScreen<SewingBasketMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/zhenxianpoluo_gui.png");
    private static final String PREFIX = "gui.yvanchuxiuzhen.zhenxianpoluo_gui.";

    public SewingBasketScreen(SewingBasketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable(PREFIX + "button_empty"), button -> send(0))
                .bounds(leftPos + 15, topPos + 16, 35, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(PREFIX + "button_empty1"), button -> send(1))
                .bounds(leftPos + 123, topPos + 16, 35, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(PREFIX + "button_zhi_zuo"), button -> send(2))
                .bounds(leftPos + 69, topPos + 37, 35, 20).build());
    }

    private void send(int button) {
        PacketDistributor.sendToServer(new SewingBasketPayload(menu.containerId, button));
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
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable(PREFIX + "label_xu_qiu_cai_liao"), 56, 68, -16777216, false);
    }
}
