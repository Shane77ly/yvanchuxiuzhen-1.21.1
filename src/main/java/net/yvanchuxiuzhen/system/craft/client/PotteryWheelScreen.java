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
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelMenu;
import net.yvanchuxiuzhen.system.craft.network.PotteryWheelButtonPayload;

/** 陶轮保留来源九槽、十一按钮与材料文案位置，服务端拥有选择与制作真值。 */
public final class PotteryWheelScreen extends AbstractContainerScreen<PotteryWheelMenu> {
    public PotteryWheelScreen(PotteryWheelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addButton(3, 56, 11, 11, "taolunguianniu03", 0, Component.translatable("gui.yvanchuxiuzhen.craft.pottery_previous"));
        addButton(160, 56, 11, 11, "taolunguianniu01", 1, Component.translatable("gui.yvanchuxiuzhen.craft.pottery_next"));
        for (int index = 0; index < 8; index++) {
            String texture = "mujiangguixvanze01" + (index == 0 ? "" : Integer.toString(index));
            addButton(14 + index * 18, 51, 20, 20, texture, index + 2,
                    Component.translatable("gui.yvanchuxiuzhen.craft.pottery_select", index + 1));
        }
        addButton(79, 38, 16, 8, "taolunguianniu05", 10, Component.translatable("gui.yvanchuxiuzhen.craft.pottery_make"));
    }

    private void addButton(int x, int y, int width, int height, String texture, int operation, Component label) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                resource("textures/screens/atlas/imagebutton_" + texture + ".png"), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new PotteryWheelButtonPayload(menu.containerId, operation)));
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
        graphics.blit(resource("textures/screens/taolungui01.png"), leftPos - 1, topPos - 9, 0, 0, 176, 81, 176, 81);
        graphics.blit(resource("textures/screens/wupinlan.png"), leftPos, topPos + 72, 0, 0, 176, 94, 176, 94);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, menu.outputName(), 2, -5, -13312, false);
        graphics.drawString(font, menu.costLabel(), 2, 15, -3355444, false);
        graphics.drawString(font, menu.costAmount(), 2, 26, -3355444, false);
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
