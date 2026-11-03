package net.yvanchuxiuzhen.system.relation.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.relation.content.FriendMenu;
import net.yvanchuxiuzhen.system.relation.data.FriendRow;
import net.yvanchuxiuzhen.system.relation.data.PlayerRelation;
import net.yvanchuxiuzhen.system.relation.data.RelationAttachments;
import net.yvanchuxiuzhen.system.relation.network.RelationButtonPayload;

/**
 * 好友界面（来源 {@code FriendguiScreen}），坐标沿用来源。左侧 10 行好友（在线标记、选中框、星标），
 * 右侧为选中好友的名字、星标、排列与删除按钮、在线时的人物与属性；底部翻页与页码。
 * 显示内容全部读本人的关系附件，由服务端刷新后同步。名字输入框在来源中没有对应按钮，只保留输入。
 */
public final class FriendScreen extends AbstractContainerScreen<FriendMenu> {
    private static final ResourceLocation BACKGROUND = texture("friendgui");
    private static final ResourceLocation ONLINE = texture("zaixiandisplay");
    private static final ResourceLocation SELECTED = texture("friendguixvanze");
    private static final ResourceLocation STAR = texture("friendjiajing03");
    private static final int ROWS = 10;
    private static final int ROW_STEP = 24;
    private EditBox playerName;

    public FriendScreen(FriendMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }

    private PlayerRelation relation() {
        return minecraft.player.getData(RelationAttachments.PLAYER);
    }

    @Override
    protected void init() {
        super.init();
        playerName = new EditBox(font, leftPos + 109, topPos + 198, 120, 20, Component.translatable("gui.yvanchuxiuzhen.friendgui.playername"));
        playerName.setMaxLength(32767);
        playerName.setHint(Component.translatable("gui.yvanchuxiuzhen.friendgui.playername"));
        addRenderableWidget(playerName);
        for (int row = 0; row < ROWS; row++) {
            String suffix = row == 0 ? "" : String.valueOf(row);
            addButton(-55, -43 + row * ROW_STEP, 157, 22, "imagebutton_friendplayerliebiao01" + suffix, row);
        }
        addButton(-55, 204, 36, 15, "imagebutton_yanggugui02", 10);
        addButton(65, 204, 36, 15, "imagebutton_yanggugui04", 11);
        addButton(214, -45, 18, 18, "imagebutton_friendjiajing01", 12);
        addButton(213, -22, 20, 20, "imagebutton_friendpailie03", 13);
        addButton(213, 3, 20, 20, "imagebutton_friendpailie05", 14);
        addButton(213, 76, 20, 20, "imagebutton_friendpailie07", 15);
    }

    private void addButton(int x, int y, int width, int height, String name, int button) {
        addRenderableWidget(new TextureButton(leftPos + x, topPos + y, width, height, texture("atlas/" + name), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new RelationButtonPayload(button))));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Player shown = selectedOnlinePlayer();
        if (shown != null) {
            int x = leftPos + 164;
            int y = topPos + 92;
            // 来源以脚下为基准点、正面朝前绘制；1.21.1 以框中心为基准，框中心放在脚下并用纵向偏移抵消半身高。
            InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, x - 60, y - 130, x + 60, y + 130, 60,
                    -shown.getBbHeight() / 2 / shown.getScale(), 0, 0, shown);
        }
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /** 选中行的好友在本客户端可见的玩家中按显示名查找（来源 {@code Playeruientity}）。 */
    private Player selectedOnlinePlayer() {
        PlayerRelation relation = relation();
        if (relation.selected() < 1) {
            return null;
        }
        String name = relation.row(relation.selected()).name();
        for (Player player : minecraft.level.players()) {
            if (player.getDisplayName().getString().equals(name)) {
                return player;
            }
        }
        return null;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        PlayerRelation relation = relation();
        guiGraphics.blit(BACKGROUND, leftPos - 60, topPos - 73, 0, 0, 300, 300, 300, 300);
        for (int index = 1; index <= ROWS; index++) {
            FriendRow row = relation.row(index);
            int offset = (index - 1) * ROW_STEP;
            if (row.online()) {
                guiGraphics.blit(ONLINE, leftPos - 51, topPos - 36 + offset, 0, 0, 8, 8, 8, 8);
            }
            if (relation.selected() == index) {
                guiGraphics.blit(SELECTED, leftPos + 41, topPos - 42 + offset, 0, 0, 60, 20, 60, 20);
            }
            if (row.star()) {
                guiGraphics.blit(STAR, leftPos + 81, topPos - 41 + offset, 0, 0, 18, 18, 18, 18);
            }
        }
        if (relation.selected() >= 1 && relation.row(relation.selected()).star()) {
            guiGraphics.blit(STAR, leftPos + 214, topPos - 45, 0, 0, 18, 18, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PlayerRelation relation = relation();
        for (int index = 1; index <= ROWS; index++) {
            FriendRow row = relation.row(index);
            String color = row.online() ? "§f" : "§8";
            guiGraphics.drawString(font, color + row.level(), -41, -35 + (index - 1) * ROW_STEP, -1, false);
        }
        guiGraphics.drawString(font, (relation.page() + 1) + "页", 16, 207, -1, false);
        if (relation.selected() >= 1) {
            FriendRow selected = relation.row(relation.selected());
            guiGraphics.drawString(font, "§6" + selected.level(), 116, -59, -26368, false);
            String[] lines = {selected.health(), selected.power(), selected.soul(), selected.attack(), selected.mana(), selected.soulAttack()};
            for (int line = 0; line < lines.length; line++) {
                guiGraphics.drawString(font, lines[line], 115, 101 + line * 12, -1, false);
            }
        }
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) {
            minecraft.player.closeContainer();
            return true;
        }
        if (playerName.isFocused()) {
            return playerName.keyPressed(key, scanCode, modifiers);
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
}
