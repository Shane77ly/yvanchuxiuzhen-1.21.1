package net.yvanchuxiuzhen.system.combat.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.content.DummyMenu;
import net.yvanchuxiuzhen.system.combat.network.DummyButtonPayload;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/**
 * 木人桩设置界面（来源 {@code MurenzhuangGuiScreen}），坐标与贴图沿用来源：三个防御输入框（x 51，y 45/81/117）
 * 各配一个“设置”按钮（x 15），“拿走木人桩”按钮在右上；标签显示玩家体质里记着的木人桩防御基值。
 */
public final class DummyScreen extends AbstractContainerScreen<DummyMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/murenzhuang_gui.png");
    private static final String[] KEYS = {"attack_Def", "mana_Def", "soul_Def"};
    private static final String[] LABELS = {"物理防御基值：", "法术防御基值：", "精神防御基值："};
    private final EditBox[] boxes = new EditBox[3];

    public DummyScreen(DummyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 224;
    }

    @Override
    protected void init() {
        super.init();
        for (int row = 0; row < 3; row++) {
            int y = 45 + row * 36;
            EditBox box = new EditBox(font, leftPos + 51, topPos + y, 120, 20, Component.translatable("gui." + ModId.VALUE + ".murenzhuang_gui." + new String[] {"attdef", "manadef", "souldef"}[row]));
            box.setMaxLength(32767);
            boxes[row] = addRenderableWidget(box);
            int button = row;
            addRenderableWidget(Button.builder(Component.translatable("gui." + ModId.VALUE + ".murenzhuang_gui." + new String[] {"button_s", "button_s1", "button_a"}[row]),
                    pressed -> PacketDistributor.sendToServer(new DummyButtonPayload(button, boxes[button].getValue())))
                    .bounds(leftPos + 15, topPos + y, 30, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui." + ModId.VALUE + ".murenzhuang_gui.button_asdas"),
                pressed -> PacketDistributor.sendToServer(new DummyButtonPayload(3, ""))).bounds(leftPos + 114, topPos + 9, 51, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (int row = 0; row < 3; row++) {
            guiGraphics.drawString(font, LABELS[row] + Cultivation.tizhiDouble(minecraft.player, KEYS[row]), 6, 35 + row * 36, -16777216, false);
        }
    }
}
