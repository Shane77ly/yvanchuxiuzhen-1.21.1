package net.yvanchuxiuzhen.system.gongfa.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.gongfa.content.ComprehensionMenu;
import net.yvanchuxiuzhen.system.gongfa.content.ComprehensionTableBlockEntity;
import net.yvanchuxiuzhen.system.gongfa.network.GongfaButtonPayload;

/**
 * 参悟界面（来源 {@code CanwutaiScreen}），坐标、颜色与贴图沿用来源：模式按钮（显示当前模式）、刷新按钮、开始按钮
 * （参悟模式为“开始参悟”，否则“开始继承”）、三行说明与物品栏。文字取自菜单所指位置的参悟台方块实体，尚未同步到客户端时不显示。
 */
public final class ComprehensionScreen extends AbstractContainerScreen<ComprehensionMenu> {
    private static final int TEXT = -13421773;

    public ComprehensionScreen(ComprehensionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }

    @Override
    protected void init() {
        super.init();
        addButton(211, 166, 76, 18, "imagebutton_canwutaiguianniu01", 0);
        addButton(211, 266, 76, 18, "imagebutton_canwutaiguianniu011", 1);
        addButton(294, 167, 16, 16, "imagebutton_shuaxin01", 2);
    }

    private void addButton(int x, int y, int width, int height, String name, int button) {
        addRenderableWidget(new TextureButton(leftPos + x, topPos + y, width, height, texture("atlas/" + name), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new GongfaButtonPayload(button))));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(texture("canwutaigui"), leftPos + 112, topPos + 119, 0, 0, 274, 255, 274, 255);
        for (int y : new int[] {292, 310, 328, 350}) {
            guiGraphics.blit(texture("shipintaigui05"), leftPos + 168, topPos + y, 0, 0, 162, 18, 162, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(minecraft.level.getBlockEntity(menu.pos()) instanceof ComprehensionTableBlockEntity table)) {
            return;
        }
        String mode = table.mode();
        guiGraphics.drawString(font, mode, 233, 172, -8178636, false);
        guiGraphics.drawString(font, mode.equals(ComprehensionTableBlockEntity.COMPREHEND) ? "开始参悟" : "开始继承", 233, 272, -8967124, false);
        for (int line = 0; line < 3; line++) {
            guiGraphics.drawString(font, table.text(line), 138, 212 + line * 20, TEXT, false);
        }
    }
}
