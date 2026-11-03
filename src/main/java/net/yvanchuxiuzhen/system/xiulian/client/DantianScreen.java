package net.yvanchuxiuzhen.system.xiulian.client;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.xiulian.api.BenmingProgress;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.content.DantianMenu;
import net.yvanchuxiuzhen.system.xiulian.network.XiulianButtonPayload;

/**
 * 丹田界面（来源 {@code DantianGuiScreen}），坐标、颜色与贴图沿用来源：左侧出身与体质列表（9 行、翻页），中间本命槽、
 * 金丹或元婴图与名字，右侧本命法宝的名字、等级与经验，以及五行属性熟练度。显示内容来自本人的修炼附件。
 */
public final class DantianScreen extends AbstractContainerScreen<DantianMenu> {
    private static final List<String> ROW_BUTTONS = List.of("imagebutton_dantianguianniu05", "imagebutton_dantianguianniu051",
            "imagebutton_dantianguianniu052", "imagebutton_dantianguianniu053", "imagebutton_dantianguianniu054",
            "imagebutton_dantianguianniu055", "imagebutton_dantianguianniu056", "imagebutton_dantianguianniu057",
            "imagebutton_dantianguianniu058");
    /** 属性熟练度：键、数值颜色、标签语言键，自上而下为金、水、木、火、土。 */
    private static final List<KillPoints> KILL_POINTS = List.of(
            new KillPoints("killentitypointsgold", -154, "label_jin_shu_xing_shou_lian_du"),
            new KillPoints("killentitypointswater", -10066177, "label_shui_shu_xing_shou_lian_du"),
            new KillPoints("killentitypointstree", -10027162, "label_mu_shu_xing_shou_lian_du"),
            new KillPoints("killentitypointsfire", -39322, "label_huo_shu_xing_shou_lian_du"),
            new KillPoints("killentitypointssoil", -3381505, "label_tu_xi_ling_wu"));
    private static final int GOLDEN = -13210;

    private record KillPoints(String key, int color, String label) {
    }

    public DantianScreen(DantianMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 300;
        imageHeight = 300;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }

    @Override
    protected void init() {
        super.init();
        addButton(-59, 239, 30, 12, "imagebutton_dantianguianniu01", 0);
        addButton(8, 239, 30, 12, "imagebutton_dantianguianniu03", 1);
        for (int row = 0; row < ROW_BUTTONS.size(); row++) {
            addButton(-59, 68 + row * 19, 98, 17, ROW_BUTTONS.get(row), row + 2);
        }
        addButton(28, 48, 11, 11, "imagebutton_dantianguianniu07", 11);
        addButton(227, 45, 13, 13, "imagebutton_dantianguianniu09", 12);
        addButton(347, 66, 11, 11, "imagebutton_dantianguianniu071", 13);
        addButton(346, 135, 11, 11, "imagebutton_dantianguianniu072", 14);
    }

    private void addButton(int x, int y, int width, int height, String name, int button) {
        addRenderableWidget(new TextureButton(leftPos + x, topPos + y, width, height, texture("atlas/" + name), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new XiulianButtonPayload(button))));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        Player player = minecraft.player;
        double level = Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
        blit(guiGraphics, "dantiangui", -63, 30, 426, 240);
        blit(guiGraphics, "dantianguiwupinlan", 68, 185, 162, 76);
        blit(guiGraphics, "dantianguibenming3", 139, 98, 20, 20);
        if (level >= 21) {
            blit(guiGraphics, "dantianguibenming", 135, 94, 28, 28);
        }
        blit(guiGraphics, "dantianguinamexianshi", 120, 166, 58, 17);
        if (Cultivation.tizhiBoolean(player, "benmingxianshi") && level >= 21 && Cultivation.benmingProgress(player).owned()) {
            blit(guiGraphics, "dantianguibenming2", 134, 91, 30, 34);
        }
        // 来源对每个打开的金丹（结丹期）或元婴（元婴期起）各画一次图，按编号顺序叠加。
        int from = level < 31 ? 101 : 201;
        int to = level < 31 ? 134 : 208;
        for (int number = from; number <= to; number++) {
            if (Cultivation.tizhiBoolean(player, "tizhiopen_" + number)) {
                blit(guiGraphics, "tizhiopen_" + number, 131, 126, 36, 36);
            }
        }
    }

    private void blit(GuiGraphics guiGraphics, String name, int x, int y, int width, int height) {
        guiGraphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Player player = minecraft.player;
        guiGraphics.drawString(font, coreName(player), 129, 171, GOLDEN, false);
        guiGraphics.drawString(font, Cultivation.get(player, CultivationText.BIRTH), -59, 50, -1, false);
        for (int row = 1; row <= 9; row++) {
            String id = Cultivation.tizhiString(player, "tizhixianshi" + row + "id");
            if (!id.isEmpty()) {
                guiGraphics.drawString(font, Component.translatable("tizhi." + id), -56, 73 + (row - 1) * 19, -1, false);
            }
        }
        guiGraphics.drawString(font, whole(Cultivation.tizhiDouble(player, "tizhipagenum")) + "/"
                + whole(Cultivation.tizhiDouble(player, "tizhipagenummax")), -20, 242, -1, false);
        BenmingProgress progress = Cultivation.benmingProgress(player);
        guiGraphics.drawString(font, Cultivation.benming(player).getDisplayName().getString(), 262, 55, GOLDEN, false);
        guiGraphics.drawString(font, "本命法宝等级：" + whole(progress.level()), 262, 68, -10053121, false);
        guiGraphics.drawString(font, whole(progress.exp()) + "/" + whole(progress.expMax()), 262, 81, -1, false);
        for (int i = 0; i < KILL_POINTS.size(); i++) {
            KillPoints points = KILL_POINTS.get(i);
            guiGraphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.dantian_gui." + points.label()), 261, 138 + i * 23, -1, false);
            guiGraphics.drawString(font, whole(Cultivation.tizhiDouble(player, points.key())), 261, 148 + i * 23, points.color(), false);
        }
    }

    /** 金丹或元婴名（来源 {@code Jindanname}）：结丹期显示金丹名，元婴期起显示元婴名，之前显示所在大境界。 */
    private static Component coreName(Player player) {
        double level = Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
        if (level >= 21 && level <= 30) {
            return Component.translatable("tizhi." + Cultivation.tizhiString(player, "jindanname"));
        }
        if (level >= 31) {
            return Component.translatable("tizhi." + Cultivation.tizhiString(player, "yvanyingname"));
        }
        return Component.literal(level <= 10 ? "--练气--" : "--筑基--");
    }

    /** 来源用 {@code DecimalFormat("0")} 显示，按四舍六入五成双取整。 */
    private static String whole(double value) {
        return new DecimalFormat("0").format(value);
    }
}
