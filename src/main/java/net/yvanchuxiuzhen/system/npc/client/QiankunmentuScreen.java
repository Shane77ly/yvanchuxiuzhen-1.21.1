package net.yvanchuxiuzhen.system.npc.client;

import java.text.DecimalFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.npc.content.QiankunmentuBlockEntity;
import net.yvanchuxiuzhen.system.npc.content.QiankunmentuMenu;

/** 门徒任务编号设置页，保留来源176×166画布与原生加号按钮。 */
public final class QiankunmentuScreen extends AbstractContainerScreen<QiankunmentuMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/qiankunmentu_gui.png");
    public QiankunmentuScreen(QiankunmentuMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 176; imageHeight = 166; }
    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.yvanchuxiuzhen.qiankunmentu_gui.button_empty"),
                ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0)).bounds(leftPos + 15, topPos + 43, 30, 20).build());
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (minecraft.level.getBlockEntity(menu.pos) instanceof QiankunmentuBlockEntity entity) {
            graphics.drawString(font, "§6第" + new DecimalFormat("0").format(entity.renwuset()) + "个任务", 15, 25, -12829636, false);
        }
    }
}
