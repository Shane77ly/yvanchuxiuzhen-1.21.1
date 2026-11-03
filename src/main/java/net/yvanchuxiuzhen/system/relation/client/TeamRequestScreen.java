package net.yvanchuxiuzhen.system.relation.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.relation.content.TeamRequestMenu;
import net.yvanchuxiuzhen.system.relation.data.RelationAttachments;
import net.yvanchuxiuzhen.system.relation.network.RelationButtonPayload;

/** 好友申请界面（来源 {@code ZuduiteamGuiScreen}）：申请者名字与同意、拒绝两个按钮，坐标沿用来源。 */
public final class TeamRequestScreen extends AbstractContainerScreen<TeamRequestMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/zuduiteam_gui.png");

    public TeamRequestScreen(TeamRequestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 70;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.yvanchuxiuzhen.zuduiteam_gui.button_yes"),
                pressed -> PacketDistributor.sendToServer(new RelationButtonPayload(0))).bounds(leftPos + 15, topPos + 40, 40, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.yvanchuxiuzhen.zuduiteam_gui.button_noo"),
                pressed -> PacketDistributor.sendToServer(new RelationButtonPayload(1))).bounds(leftPos + 114, topPos + 40, 40, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String name = minecraft.player.getData(RelationAttachments.PLAYER).requestName();
        guiGraphics.drawString(font, "是否接受来自" + name + "的好友申请？", 6, 22, -16777216, false);
    }
}
