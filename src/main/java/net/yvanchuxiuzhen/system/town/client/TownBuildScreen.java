package net.yvanchuxiuzhen.system.town.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.town.content.TownBlockEntity;
import net.yvanchuxiuzhen.system.town.content.TownBuildMenu;
import net.yvanchuxiuzhen.system.town.data.TownBuildPlan;

/** 来源十八槽建设页展示已拥有数量与建造按钮。 */
public final class TownBuildScreen extends AbstractContainerScreen<TownBuildMenu> {
    public TownBuildScreen(TownBuildMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("建造"), ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0))
                .bounds(leftPos + 132, topPos + 8, 35, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/chengzhenbuild_gui.png"), leftPos, topPos,
                0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!(minecraft.level.getBlockEntity(menu.pos) instanceof TownBlockEntity town)) return;
        TownBuildPlan build = town.buildPlan();
        graphics.drawString(font, build.buildName(), 6, 8, 0xFF9900, false);
        String[] names = {"原木", "石料", "羊毛"};
        for (int index = 0; index < 3; index++) graphics.drawString(font, "需求" + names[index] + "（已拥有）："
                + build.need(index) + "（" + build.owned(index) + "）",
                6, 26 + index * 9, 0x3C3C3C, false);
    }
}
