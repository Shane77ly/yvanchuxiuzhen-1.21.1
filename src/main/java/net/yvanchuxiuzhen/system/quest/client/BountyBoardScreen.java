package net.yvanchuxiuzhen.system.quest.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.quest.content.BountyBoardBlockEntity;
import net.yvanchuxiuzhen.system.quest.content.BountyBoardMenu;

/** 来源500方形画布中的六行悬赏目录与背包，整行按钮直接开始追踪。 */
public final class BountyBoardScreen extends AbstractContainerScreen<BountyBoardMenu> {
    public BountyBoardScreen(BountyBoardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        for (int index = 0; index < 6; index++) {
            final int button = index;
            TextureButton row = new TextureButton(leftPos + 133, topPos + 142 + index * 16, 232, 16,
                    texture("atlas/imagebutton_xvanshangguianniu01" + (index == 0 ? "" : index)), 232, 32,
                    ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button));
            row.setMessage(Component.literal("领取第" + (index + 1) + "项悬赏"));
            addRenderableWidget(row);
        }
        TextureButton information = new TextureButton(leftPos + 382, topPos + 144, 12, 12, texture("atlas/imagebutton_bankgui10"), 12, 24,
                ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 6));
        information.setMessage(Component.literal("悬赏说明"));
        addRenderableWidget(information);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture("wupinlan"), leftPos + 162, topPos + 239, 0, 0, 176, 94, 176, 94);
        graphics.blit(texture("xvanshanggui"), leftPos + 120, topPos + 132, 0, 0, 258, 120, 258, 120);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!(minecraft.level.getBlockEntity(menu.pos) instanceof BountyBoardBlockEntity board)) return;
        for (int index = 1; index <= 6; index++) {
            CompoundTag entry = board.entry(index);
            String text = "§4" + Component.translatable(entry.getString("entityname")).getString() + "(LV:" + entry.getInt("LEVELnumber")
                    + " 赏金:" + entry.getInt("xvanshangmoney") + ")距离:" + entry.getInt("zuobiaojvli") + "米"
                    + (entry.getBoolean("zhuizhong") ? "(进行)" : "");
            graphics.drawString(font, text, 169, 145 + (index - 1) * 16, 0x990000, false);
        }
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
