package net.yvanchuxiuzhen.panel.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.panel.PlayerPanelMenu;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 玩家属性汇总：来源布局与显示格式，所有数值只读 attribute/xiulian 本人同步数据。 */
public final class AttributeScreen extends AbstractContainerScreen<PlayerPanelMenu> {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");
    private static final DecimalFormat REALM = new DecimalFormat("00");
    private static final List<CultivationValue> ROOTS = List.of(CultivationValue.ROOT_GOLD, CultivationValue.ROOT_WATER,
            CultivationValue.ROOT_TREE, CultivationValue.ROOT_FIRE, CultivationValue.ROOT_SOIL);
    private static final List<String> NAVIGATION = List.of("属性显示", "属性加点", "饰品装备", "功法与技能", "副职业", "修炼");
    private static final int TEXT = -10079488;

    public AttributeScreen(PlayerPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        addButton(69, 201, 66, 18, "guipage012", 1, "饰品装备");
        addButton(366, 156, 12, 12, "bankgui10", 5, "属性说明");
        addButton(360, 173, 24, 24, "dantianguianniu11", 6, "丹田");
    }

    private void addButton(int x, int y, int width, int height, String name, int id, String message) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height, texture("atlas/imagebutton_" + name), width, height * 2,
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
        blit(graphics, "gui01", 163, 159, 99, 85);
        blit(graphics, "guipage03", 69, 147, 66, 18);
        for (int y : new int[]{250, 268, 286, 308}) blit(graphics, "shipintaigui05", 169, y, 162, 18);
        for (int index = 0; index < ROOTS.size(); index++) {
            if (Cultivation.get(minecraft.player, ROOTS.get(index)) > 0) blit(graphics, "gui010" + (index + 1), 320, 157 + index * 14, 14, 14);
        }
        double yinYang = Cultivation.get(minecraft.player, CultivationValue.YINYANG);
        if (yinYang > 0) blit(graphics, "gui0106", 320, 227, 14, 14);
        else if (yinYang < 0) blit(graphics, "gui0107", 320, 227, 14, 14);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Player player = minecraft.player;
        String realm = Component.translatable("levelname" + REALM.format(Cultivation.get(player, CultivationValue.LEVEL_NUMBER))).getString();
        graphics.drawString(font, realm + " " + player.getDisplayName().getString() + "§b(" + WHOLE.format(Cultivation.get(player, CultivationValue.EXPERIENCE))
                + "/" + WHOLE.format(Cultivation.get(player, CultivationValue.EXPERIENCE_MAX)) + ")", 169, 162, -3368704, false);
        List<String> lines = List.of(
                "气血：" + WHOLE.format(player.getHealth()) + "/" + WHOLE.format(player.getMaxHealth()) + recovery(player, Stat.HEALTH_REC, "HealthRestorePercentage"),
                "灵力：" + WHOLE.format(Cultivation.get(player, CultivationValue.POWER_DISPLAY)) + "/" + WHOLE.format(PlayerAttributes.value(player, Stat.POWER_MAX))
                        + recovery(player, Stat.POWER_REC, "PowerRestorePercentage"),
                "精力：" + WHOLE.format(Cultivation.get(player, CultivationValue.SOUL_DISPLAY)) + "/" + PlayerAttributes.value(player, Stat.SOUL_MAX)
                        + recovery(player, Stat.SOUL_REC, "JingliRestorePercentage"),
                attack(player, "物理", Stat.ATTACK, Stat.ATTACK_DEF), attack(player, "法术", Stat.MANA_ATT, Stat.MANA_DEF),
                attack(player, "精神", Stat.SOUL_ATT, Stat.SOUL_DEF), signed(Cultivation.get(player, CultivationValue.VIGOROUS), "罡气", "罡煞等分", "煞气"));
        for (int index = 0; index < lines.size(); index++) graphics.drawString(font, lines.get(index), 174, 176 + index * 10, TEXT, false);
        graphics.drawString(font, signed(Cultivation.get(player, CultivationValue.MERITS), "功德", "功业持平", "业力"), 263, 236, TEXT, false);
        for (int index = 0; index < ROOTS.size(); index++) {
            double root = Cultivation.get(player, ROOTS.get(index));
            if (root > 0) graphics.drawString(font, WHOLE.format(root), 325, 161 + index * 14, TEXT, false);
        }
        double yinYang = Cultivation.get(player, CultivationValue.YINYANG);
        String yinYangText = yinYang > 0 ? "§0" + WHOLE.format(yinYang) : yinYang < 0 ? "§f" + WHOLE.format(-yinYang) : "";
        graphics.drawString(font, yinYangText, 325, 231, -12829636, false);
        for (int index = 0; index < NAVIGATION.size(); index++) {
            graphics.drawString(font, NAVIGATION.get(index), 72, 152 + index * 27, index == 0 ? -6710887 : -1, false);
        }
    }

    private static String recovery(Player player, Stat stat, String percentage) {
        return "(" + PlayerAttributes.value(player, stat) + ")(" + WHOLE.format(Cultivation.tizhiDouble(player, percentage) * 100) + "%)";
    }

    private static String attack(Player player, String name, Stat attack, Stat defense) {
        return name + "攻击：" + WHOLE.format(PlayerAttributes.value(player, attack)) + " " + name + "防御：" + WHOLE.format(PlayerAttributes.value(player, defense));
    }

    private static String signed(double value, String positive, String zero, String negative) {
        return value > 0 ? positive + "：" + WHOLE.format(value) : value < 0 ? negative + "：" + WHOLE.format(-value) : zero;
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
