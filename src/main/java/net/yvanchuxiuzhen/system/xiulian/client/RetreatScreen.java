package net.yvanchuxiuzhen.system.xiulian.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.content.RetreatMenu;

/** 来源06/07/08三页的固定布局，寿元和设定天数只读本人修炼快照。 */
public final class RetreatScreen extends AbstractContainerScreen<RetreatMenu> {
    private static final List<String> NAVIGATION = List.of("shu_xing_xian_shi", "shu_xing_jia_dian", "shi_pin_zhuang_bei",
            "gong_fa_yu_ji_neng", "fu_zhi_ye", "xiu_lian");
    private static final List<String> NAVIGATION_NAMES = List.of("属性显示", "属性加点", "饰品装备", "功法与技能", "副职业");
    private static final DecimalFormat DAYS = new DecimalFormat("0");

    public RetreatScreen(RetreatMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        int mode = menu.mode();
        addButton(216, 159, 66, 74, "gui06xiulian", 0, mode == 7 ? "观想" : mode == 8 ? "开始参悟功法" : "开始修炼");
        if (mode != 7) {
            addButton(260, 235, 7, 8, "gui06jia101", 1, "增加一天");
            addButton(268, 235, 12, 8, "gui06jia201", 2, "增加十天");
            addButton(281, 235, 17, 8, "gui06jia301", 3, "增加一百天");
            addButton(231, 235, 7, 8, "gui06jian101", 4, "减少一天");
            addButton(218, 235, 12, 8, "gui06jian201", 5, "减少十天");
            addButton(200, 235, 17, 8, "gui06jian301", 6, "减少一百天");
        }
        for (int index = 0; index < 5; index++) {
            addButton(69, 147 + index * 27, 66, 18, "guipage01" + (index == 0 ? "" : index),
                    (mode == 7 ? 1 : 7) + index, NAVIGATION_NAMES.get(index));
        }
        if (mode != 6) addButton(168, 192, 8, 26, "xiulianqiehuan03", mode == 7 ? 6 : 13, mode == 7 ? "修炼" : "观想");
        if (mode != 8) addButton(321, 192, 8, 26, "xiulianqiehuan01", mode == 7 ? 8 : 12, mode == 7 ? "参悟功法" : "观想");
        addButton(366, 156, 12, 12, "bankgui10", mode == 6 ? 13 : mode == 7 ? 7 : 12, mode == 7 ? "观想说明" : mode == 8 ? "功法参悟说明" : "修炼说明");
    }

    private void addButton(int x, int y, int width, int height, String name, int id, String message) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + name), width, height * 2,
                pressed -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id));
        button.setMessage(Component.literal(message));
        addRenderableWidget(button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        blit(graphics, "wupilanbiankuang", 162, 243, 176, 89);
        blit(graphics, "gui", 141, 136, 216, 130);
        switch (menu.mode()) {
            case 6 -> blit(graphics, "gui06beijingzi01", 199, 175, 100, 54);
            case 7 -> blit(graphics, "gui06beijingzi02", 201, 174, 100, 54);
            case 8 -> blit(graphics, "gui06beijingzi03", 195, 179, 104, 51);
        }
        if (menu.mode() != 7) blit(graphics, "gui06tianshu", 239, 235, 20, 8);
        blit(graphics, "guipage03", 69, 282, 66, 18);
        for (int y : new int[]{250, 268, 286, 308}) blit(graphics, "shipintaigui05", 169, y, 162, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "寿命：" + DAYS.format(Cultivation.get(minecraft.player, CultivationValue.LIFETIME)) + "天", 161, 158, -6724096, false);
        if (menu.mode() != 7) {
            graphics.drawString(font, DAYS.format(Cultivation.tizhiDouble(minecraft.player, "xiuliandaynum")), 240, 236, -6724096, false);
        }
        for (int index = 0; index < NAVIGATION.size(); index++) {
            graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.player_gui_0" + menu.mode() + ".label_" + NAVIGATION.get(index)),
                    72, 152 + index * 27, index == 5 ? -6710887 : -1, false);
        }
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
