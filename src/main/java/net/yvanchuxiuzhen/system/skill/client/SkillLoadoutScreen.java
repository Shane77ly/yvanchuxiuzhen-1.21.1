package net.yvanchuxiuzhen.system.skill.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.content.SkillLoadoutMenu;

/** 功法与技能装配页；槽位由原生容器同步，名称读取各系统已经保存的本人数据。 */
public final class SkillLoadoutScreen extends AbstractContainerScreen<SkillLoadoutMenu> {
    private static final List<String> NAVIGATION = List.of("shu_xing_xian_shi", "shu_xing_jia_dian", "shi_pin_zhuang_bei",
            "gong_fa_yu_ji_neng", "fu_zhi_ye", "xiu_lian");
    private static final int TEXT = -10079488;

    public SkillLoadoutScreen(SkillLoadoutMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        addButton(295, 193, 8, 10, "gui0401down01", 0, "上一页技能");
        addButton(325, 193, 8, 10, "gui0401up01", 1, "下一页技能");
        addButton(69, 147, 66, 18, "guipage01", 2, "属性显示");
        addButton(69, 201, 66, 18, "guipage012", 4, "饰品装备");
        addButton(366, 156, 12, 12, "bankgui10", 7, "装配说明");
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
        blit(graphics, "gui04", 165, 159, 168, 84);
        blit(graphics, "gui0601", 233, 205, 18, 18);
        blit(graphics, "guipage03", 69, 228, 66, 18);
        for (int y : new int[] {250, 268, 286, 308}) blit(graphics, "shipintaigui05", 169, y, 162, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int index = 0; index < NAVIGATION.size(); index++) {
            graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.player_gui_04.label_" + NAVIGATION.get(index)),
                    72, 152 + index * 27, index == 3 ? -6710887 : -1, false);
        }
        graphics.drawString(font, Integer.toString(Skills.state(minecraft.player).page()), 311, 194, TEXT, false);
        graphics.drawString(font, savedName(Techniques.slot(minecraft.player), "无功法"), 199, 161, TEXT, false);
        graphics.drawString(font, savedName(Skills.movement(minecraft.player), "无轻功"), 185, 212, TEXT, false);
        graphics.drawString(font, savedName(Skills.flight(minecraft.player), "无飞行技能"), 185, 232, TEXT, false);
        graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.player_gui_04.label_wu_qi_fa_men"), 253, 212, TEXT, false);
    }

    private static String savedName(ItemStack stack, String empty) {
        return stack.isEmpty() ? empty : stack.getDisplayName().getString();
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
