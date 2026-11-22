package net.yvanchuxiuzhen.system.xiulian.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.SkillPoint;
import net.yvanchuxiuzhen.system.xiulian.content.SkillPointMenu;

/** 来源属性加点页的固定布局，数值只读本人同步档案和最终属性。 */
public final class SkillPointScreen extends AbstractContainerScreen<SkillPointMenu> {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");
    private static final List<SkillPoint> POINTS = List.of(SkillPoint.HEALTH_MAX, SkillPoint.POWER_MAX,
            SkillPoint.SOUL_MAX, SkillPoint.ATTACK, SkillPoint.MANA, SkillPoint.SOUL);
    private static final List<String> NAVIGATION = List.of("shu_xing_xian_shi", "shu_xing_jia_dian", "shi_pin_zhuang_bei",
            "gong_fa_yu_ji_neng", "fu_zhi_ye", "xiu_lian");
    private static final List<String> POINT_LABELS = List.of("增加气血上限", "增加灵力上限", "增加精力上限", "增加物理攻击", "增加法术攻击", "增加精神攻击");
    private static final String LABEL_PREFIX = "gui.yvanchuxiuzhen.player_gui_02.label_";

    public SkillPointScreen(SkillPointMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        for (int index = 0; index < POINTS.size(); index++) {
            addButton(165, 176 + index * 10, 8, 8, "gui020" + (index + 1), index, Component.literal(POINT_LABELS.get(index)));
        }
        addButton(69, 147, 66, 18, "guipage01", 6, label(0));
        addButton(69, 201, 66, 18, "guipage012", 7, label(2));
        addButton(69, 228, 66, 18, "guipage013", 8, label(3));
        addButton(69, 255, 66, 18, "guipage014", 9, label(4));
        addButton(69, 282, 66, 18, "guipage011", 10, label(5));
        addButton(366, 156, 12, 12, "bankgui10", 11, Component.literal("加点说明"));
    }

    private void addButton(int x, int y, int width, int height, String name, int id, Component message) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height, texture("atlas/imagebutton_" + name), width, height * 2,
                pressed -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id));
        button.setMessage(message);
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
        blit(graphics, "gui02", 166, 164, 6, 6);
        blit(graphics, "guipage03", 69, 174, 66, 18);
        for (int y : new int[]{250, 268, 286, 308}) blit(graphics, "shipintaigui05", 169, y, 162, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Player player = minecraft.player;
        graphics.drawString(font, Component.translatable("gui02shuxingdian").getString()
                + WHOLE.format(Cultivation.get(player, CultivationValue.SKILL_POINTS)), 174, 163, -16777012, false);
        for (int index = 0; index < POINTS.size(); index++) {
            SkillPoint point = POINTS.get(index);
            String key = "gui02jiadian0" + (index + 1);
            double current = point == SkillPoint.HEALTH_MAX ? player.getAttributeValue(Attributes.MAX_HEALTH) : PlayerAttributes.value(player, point.stat());
            String text = Component.translatable(key).getString() + WHOLE.format(point.amount() + Cultivation.tizhiDouble(player, point.correctionKey()))
                    + Component.translatable(key + "01").getString() + WHOLE.format(current)
                    + "(" + WHOLE.format(Cultivation.get(player, point.spent())) + ")";
            graphics.drawString(font, text, 174, 176 + index * 10, -10079488, false);
        }
        for (int index = 0; index < NAVIGATION.size(); index++) {
            graphics.drawString(font, label(index), 72, 152 + index * 27, index == 1 ? -6710887 : -1, false);
        }
    }

    private static Component label(int index) {
        return Component.translatable(LABEL_PREFIX + NAVIGATION.get(index));
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
