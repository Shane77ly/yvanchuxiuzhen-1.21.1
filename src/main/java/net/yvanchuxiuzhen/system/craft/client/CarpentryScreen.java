package net.yvanchuxiuzhen.system.craft.client;

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
import net.yvanchuxiuzhen.system.craft.content.CarpentryMenu;
import net.yvanchuxiuzhen.system.craft.network.CarpentryButtonPayload;

/** 木匠台保留来源候选区、五行材料与43个按钮，按钮按当前菜单身份交给服务端执行。 */
public final class CarpentryScreen extends AbstractContainerScreen<CarpentryMenu> {
    public CarpentryScreen(CarpentryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addButton(141, 7, 18, 18, "mujiangguianniu01", 0, Component.literal("制作"));
        for (int index = 0; index < 40; index++) {
            String texture = "mujiangguixvanze01" + (index == 0 ? "" : Integer.toString(index));
            addButton(-102 + index % 5 * 18, -26 + index / 5 * 18, 20, 20, texture, index + 1,
                    Component.literal("选择第 " + (index + 1) + " 项"));
        }
        addButton(-29, 124, 20, 13, "huafutai_up01", 41, Component.literal("切换类别"));
        addButton(-101, 124, 20, 13, "huafutai_down01", 42, Component.literal("切换类别"));
    }

    private void addButton(int x, int y, int width, int height, String texture, int operation, Component label) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                resource("textures/screens/atlas/imagebutton_" + texture + ".png"), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new CarpentryButtonPayload(menu.containerId, operation)));
        button.setMessage(label);
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
        graphics.blit(resource("textures/screens/mujiang_gui.png"), leftPos, topPos, 0, 0,
                imageWidth, imageHeight, imageWidth, imageHeight);
        graphics.blit(resource("textures/screens/mujianggui.png"), leftPos - 107, topPos - 56, 0, 0,
                296, 222, 296, 222);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, menu.outputName(), 51, 7, -26368, false);
        for (int row = 0; row < 5; row++) {
            graphics.drawString(font, menu.requirementName(row), 51, 25 + row * 9, -12829636, false);
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
