package net.yvanchuxiuzhen.system.town.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.town.content.TownBlockEntity;
import net.yvanchuxiuzhen.system.town.content.TownMenu;
import net.yvanchuxiuzhen.system.town.data.TownBuildRegistry;

/** 五个来源城镇展示页；空监听按钮保持来源没有动作的边界。 */
public final class TownScreen extends AbstractContainerScreen<TownMenu> {
    public TownScreen(TownMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        if (menu.page == 1) button(15, 16, 20, 20, "chengzhenui02", "占领");
        if (menu.page == 5) {
            button(11, -20, 17, 16, "renwulan04", "教头");
            button(11, 0, 17, 16, "renwulan041", "参谋");
        }
    }

    private void button(int x, int y, int width, int height, String texture, String name) {
        TextureButton button = new TextureButton(leftPos + x, topPos + y, width, height, texture("atlas/imagebutton_" + texture), width, height * 2, ignored -> { });
        button.setMessage(Component.literal(name));
        addRenderableWidget(button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture("chengzhen_ui_0" + menu.page), leftPos, topPos, 0, 0, 176, 166, 176, 166);
        if (menu.page >= 2) graphics.blit(texture("chengzhenui01"), leftPos - 14, topPos - 45, 0, 0, 204, 140, 204, 140);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!(minecraft.level.getBlockEntity(menu.pos) instanceof TownBlockEntity town)) return;
        if (menu.page == 1) {
            text(graphics, "占领", 40, 22, 0x990033);
            text(graphics, "点击此按钮后将开启占领之战", 15, 52, 0x3C3C3C);
        } else if (menu.page == 2) {
            text(graphics, town.townName() + "（" + town.townLevelName() + "）", 15, -20, 0xFFCC66);
            text(graphics, "常驻人口：" + town.residentPopulation(), 15, -2, 0x333333);
            text(graphics, "住房和洞府数量：" + town.houseNumber() + "座（容量" + town.housePopulation() + "）", 15, 7, 0x333333);
            text(graphics, "流动人口：" + town.floatingPopulation(), 15, 25, 0x333333);
            text(graphics, "旅馆和驿站的数量：" + town.hotelNumber() + "座（容量" + town.hotelPopulation() + "）", 15, 34, 0x333333);
            text(graphics, "死亡人数：" + town.deathCount(), 15, 52, 0x333333);
        } else if (menu.page == 3) {
            text(graphics, "建筑列表", 6, -20, 0xFFCC66);
            TownBuildRegistry build = town.townBuild();
            StringBuilder list = new StringBuilder();
            for (int index = 1; index <= 5; index++) {
                String key = "txt.yvanchuxiuzhen.build0" + index;
                int amount = build.countOf(key);
                if (index > 1 && amount < 1) continue;
                if (!list.isEmpty()) list.append('、');
                list.append(Component.translatable(key).getString()).append('（').append(amount).append('）');
            }
            for (int line = 0; line < 8 && line * 18 < list.length(); line++) text(graphics,
                    list.substring(line * 18, Math.min((line + 1) * 18, list.length())), 6, -11 + line * 9, 0x3C3C3C);
        } else if (menu.page == 4) {
            text(graphics, "常驻军队：" + town.troopCount() + "位", 15, 7, 0x333333);
            text(graphics, "常驻修者总数：" + town.residentCultivators() + "位", 15, 25, 0x333333);
            text(graphics, "路过修者：" + town.floatingCultivators() + "位", 15, 43, 0x333333);
        } else {
            text(graphics, town.townJiaotou(), 32, -16, 0x3C3C3C);
            text(graphics, town.townCanmou(), 31, 4, 0x3C3C3C);
        }
    }

    private void text(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, false);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
