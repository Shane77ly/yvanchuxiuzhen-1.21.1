package net.yvanchuxiuzhen.system.profession.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.profession.content.ProfessionMenu;

/** 来源 PlayerGUI05 的双列十职业显示，文本只读职业公开查询。 */
public final class ProfessionScreen extends AbstractContainerScreen<ProfessionMenu> {
    private static final List<String> NAVIGATION = List.of("shu_xing_xian_shi", "shu_xing_jia_dian", "shi_pin_zhuang_bei",
            "gong_fa_yu_ji_neng", "fu_zhi_ye", "xiu_lian");
    private static final int TEXT = -10079488;

    public ProfessionScreen(ProfessionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        addButton(69, 147, 66, 18, "guipage01", 0, "属性显示");
        addButton(69, 174, 66, 18, "guipage011", 1, "属性加点");
        addButton(69, 201, 66, 18, "guipage012", 2, "饰品装备");
        addButton(69, 228, 66, 18, "guipage013", 3, "功法与技能");
        addButton(69, 282, 66, 18, "guipage014", 4, "修炼");
        addButton(366, 156, 12, 12, "bankgui10", 5, "副职业说明");
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
        blit(graphics, "gui05", 167, 160, 86, 72);
        blit(graphics, "guipage03", 69, 255, 66, 18);
        for (int y : new int[]{250, 268, 286, 308}) blit(graphics, "shipintaigui05", 169, y, 162, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Profession[] professions = Profession.values();
        for (int index = 0; index < professions.length; index++) {
            int x = index < 5 ? 176 : 254;
            int y = 160 + index % 5 * 16;
            graphics.drawString(font, Professions.levelText(minecraft.player, professions[index]), x, y, TEXT, false);
            int experienceOffset = index < 2 || index >= 5 ? 11 : 12;
            graphics.drawString(font, Professions.experienceText(minecraft.player, professions[index]), x + experienceOffset, y + 8, TEXT, false);
        }
        for (int index = 0; index < NAVIGATION.size(); index++) {
            graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.player_gui_05.label_" + NAVIGATION.get(index)),
                    72, 152 + index * 27, index == 4 ? -6710887 : -1, false);
        }
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
