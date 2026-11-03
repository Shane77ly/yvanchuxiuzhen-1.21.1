package net.yvanchuxiuzhen.system.xiulian.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.content.LevelUpMenu;
import net.yvanchuxiuzhen.system.xiulian.network.XiulianButtonPayload;

/** 升级界面（来源 {@code PlayershengjixianshiScreen}）：10 行升级文字与关闭按钮，坐标与颜色沿用来源。 */
public final class LevelUpScreen extends AbstractContainerScreen<LevelUpMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/levelupxianshi.png");
    private static final ResourceLocation CLOSE = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/atlas/imagebutton_dantianguianniu09.png");

    public LevelUpScreen(LevelUpMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new TextureButton(leftPos + 194, topPos + 41, 13, 13, CLOSE, 13, 26,
                pressed -> PacketDistributor.sendToServer(new XiulianButtonPayload(0))));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos - 19, topPos + 1, 0, 0, 214, 150, 214, 150);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, line(1), 71, 46, -13261, false);
        guiGraphics.drawString(font, line(2), 114, 47, -10053121, false);
        for (int i = 3; i <= 10; i++) {
            guiGraphics.drawString(font, line(i), 23, 59 + (i - 3) * 11, -1, false);
        }
    }

    private String line(int number) {
        return Cultivation.tizhiString(minecraft.player, "uplevelxianshi" + number);
    }
}
