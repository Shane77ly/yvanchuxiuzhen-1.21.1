package net.yvanchuxiuzhen.system.quest.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.QuestDialogueSession;
import net.yvanchuxiuzhen.system.quest.content.QuestJournalMenu;

/** 来源500×500任务目录，八行选择、分类、翻页和继续共用原生菜单按钮。 */
public final class QuestJournalScreen extends AbstractContainerScreen<QuestJournalMenu> {
    private final TextureButton[] rows = new TextureButton[8];

    public QuestJournalScreen(QuestJournalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        button(0, 149, 107, 27, 36, "renwulan08", "切换任务分类");
        button(1, 316, 107, 27, 36, "renwulan10", "切换任务分类");
        button(2, 106, 376, 17, 16, "renwulan02", "上一页");
        button(3, 169, 376, 17, 16, "renwulan04", "下一页");
        QuestDialogueSession state = DialogueActions.snapshot(minecraft.player);
        for (int row = 0; row < rows.length; row++) {
            rows[row] = button(row + 4, 16, 147 + row * 28, 260, 26,
                    "renwulan12" + (row == 0 ? "" : row), state.row(row + 1).name());
        }
        button(12, 438, 156, 37, 20, "renwulan14", "继续任务");
    }

    private TextureButton button(int id, int x, int y, int width, int height, String texture, String label) {
        TextureButton button = new TextureButton(leftPos + x, topPos + y, width, height, texture("atlas/imagebutton_" + texture),
                width, height * 2, ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id));
        button.setMessage(Component.literal(label));
        return addRenderableWidget(button);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        QuestDialogueSession state = DialogueActions.snapshot(minecraft.player);
        for (int row = 0; row < rows.length; row++) rows[row].setMessage(Component.literal(state.row(row + 1).name()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        blit(graphics, "renwulan01", 0, 104, 500, 304);
        blit(graphics, "renwulan07", 171, 107, 150, 36);
        blit(graphics, "renwulan06", 125, 376, 42, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        QuestDialogueSession state = DialogueActions.snapshot(minecraft.player);
        for (int row = 1; row <= 8; row++) {
            graphics.drawString(font, state.row(row).name(), 27, 157 + (row - 1) * 28, -1, false);
            graphics.drawString(font, state.detailLine(row), 312, 183 + (row - 1) * 18, -13421773, false);
        }
        graphics.drawString(font, state.journal().selectedTitle(), 330, 165, -3368704, false);
        graphics.drawString(font, state.journal().selectedRequirement(), 312, 345, -16764007, false);
        graphics.drawString(font, state.journal().selectedReward(), 312, 363, -16737895, false);
        // 来源页码显示过程未读取玩家会话，固定从空物品标签得到0/0。
        graphics.drawString(font, "0/0", 135, 379, -1, false);
        graphics.drawString(font, state.journal().category(), 223, 123, -154, false);
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
