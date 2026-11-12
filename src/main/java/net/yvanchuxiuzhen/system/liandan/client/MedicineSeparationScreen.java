package net.yvanchuxiuzhen.system.liandan.client;

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
import net.yvanchuxiuzhen.system.liandan.content.MedicineSeparationMenu;
import net.yvanchuxiuzhen.system.liandan.network.MedicineSeparationPayload;

/** 两个操作页沿用来源画布、按钮贴图和槽位坐标，按钮只请求服务端执行操作。 */
public final class MedicineSeparationScreen extends AbstractContainerScreen<MedicineSeparationMenu> {
    private final String page;

    public MedicineSeparationScreen(MedicineSeparationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        page = menu.merging() ? "hedan" : "fendan";
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.yvanchuxiuzhen." + page + "_gui.button_"
                        + (menu.merging() ? "fen_dan" : "he_dan")), button -> send(0))
                .bounds(leftPos - 38, topPos + (menu.merging() ? 7 : 34), 35, 20).build());
        int width = menu.merging() ? 14 : 26;
        int height = menu.merging() ? 32 : 14;
        Button execute = new TextureButton(leftPos + (menu.merging() ? 80 : 74), topPos + (menu.merging() ? 18 : 36),
                width, height, texture("atlas/imagebutton_" + page + "_button01"), width, height * 2, button -> send(1));
        execute.setMessage(Component.translatable("gui.yvanchuxiuzhen." + page + "_gui.label_" + (menu.merging() ? "he_dan" : "fen_dan")));
        addRenderableWidget(execute);
    }

    private void send(int button) {
        PacketDistributor.sendToServer(new MedicineSeparationPayload(menu.containerId, button));
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
        graphics.blit(texture(page + "_gui"), leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen." + page + "_gui.label_"
                + (menu.merging() ? "he_dan" : "fen_dan")), 6, 7, -16777216, false);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
