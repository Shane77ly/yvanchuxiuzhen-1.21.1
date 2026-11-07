package net.yvanchuxiuzhen.system.skill.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.content.SkillMenu;
import net.yvanchuxiuzhen.system.skill.network.SkillButtonPayload;

/** 技能选择界面：来源四象按钮、四页、十一箭种轮盘、连招与功能按钮；数据取本人附件，操作走当前菜单消息。 */
public final class SkillScreen extends AbstractContainerScreen<SkillMenu> {
    private static final int[] ARROW_X = {57, 45, 37, 31, 27, 25, 27, 31, 37, 45, 57};
    private static final int[] ARROW_Y = {112, 137, 162, 187, 212, 237, 262, 287, 312, 337, 362};
    private static final List<String> DIRECTIONS = List.of("up", "left", "down", "right");

    public SkillScreen(SkillMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        for (int index = 0; index < 4; index++) {
            addButton(index % 2 == 0 ? 113 : 250, index < 2 ? 114 : 250, 135, 135,
                    "jinenggui" + DIRECTIONS.get(index) + "01", index, "技能 " + (index + 1));
        }
        addButton(89, 207, 16, 28, "jinengguipageup01", 4, "上一页");
        addButton(89, 263, 16, 28, "jinengguipagedown01", 5, "下一页");
        for (int index = 0; index < 11; index++) {
            String suffix = index == 0 ? "01" : "01" + index;
            addButton(ARROW_X[index], ARROW_Y[index], 24, 24, "jinengguiarrowxvanze" + suffix, index + 6, "箭种 " + (index + 1));
        }
        addButton(390, 132, 43, 26, "lianzhaoanniu01", 17, "连招");
        addButton(395, 346, 32, 32, "jinengxvkong01", 18, "虚空裂隙");
        addButton(395, 275, 32, 32, "jinengyaohua01", 19, "妖化");
        addButton(483, 345, 32, 32, "jinengzibao01", 20, "自爆");
        addButton(395, 310, 32, 32, "jinengchuqiao01", 21, "元婴出窍");
        addButton(395, 239, 32, 32, "jinengsaomiao01", 22, "神识扫描");
        addButton(395, 203, 32, 32, "jinengdanhuo01", 23, "召唤丹火");
    }

    private void addButton(int x, int y, int width, int height, String texture, int id, String name) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height, texture("atlas/imagebutton_" + texture), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new SkillButtonPayload(menu.containerId, id)));
        button.setMessage(Component.literal(name));
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
        Player player = minecraft.player;
        for (int index = 0; index < 4; index++) {
            SkillBook book = pageBook(player, index);
            if (book != null) {
                int affinity = affinity(book);
                if (affinity != 0) {
                    blit(graphics, "jinengguishuxing" + affinity + "0" + (index + 1), index % 2 == 0 ? 173 : 251, index < 2 ? 173 : 251, 75, 75);
                }
            }
        }
        for (int index = 0; index < 11; index++) {
            if (Skills.canSelectArrow(player, index + 1)) {
                if (Skills.arrowChoice(player) == index + 1) {
                    blit(graphics, "jinengguiarrowxvanze03", ARROW_X[index], ARROW_Y[index], 24, 24);
                }
                blit(graphics, "jinengguiarrow%02d".formatted(index + 1), ARROW_X[index] + 4, ARROW_Y[index] + 4, 16, 16);
            }
        }
        blit(graphics, Skills.state(player).combo() ? "lianzhaoxianshi02" : "lianzhaoxianshi01", 390, 132, 43, 26);
        int slot = Skills.state(player).selectedSlot();
        if (slot > 0 && (slot - 1) / 4 + 1 == Skills.state(player).page()) {
            int index = (slot - 1) % 4;
            blit(graphics, "jinengxvanze0" + (index + 1), index % 2 == 0 ? 227 : 251, index < 2 ? 228 : 251, 20, 20);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int index = 0; index < 4; index++) {
            SkillBook book = pageBook(minecraft.player, index);
            if (book != null) {
                int x = index == 0 || index == 2 ? 199 : index == 1 ? 263 : 264;
                graphics.drawString(font, book.name(), x, index < 2 ? 215 : 270, -1, false);
            }
        }
        graphics.drawString(font, Integer.toString(Skills.state(minecraft.player).page()), 93, 244, -1, false);
    }

    private static SkillBook pageBook(Player player, int index) {
        return Skills.book(Skills.spell(player, (Skills.state(player).page() - 1) * 4 + index + 1))
                .filter(book -> book.ownedBy(player.getUUID())).orElse(null);
    }

    /** 来源只取最先达到一秒冷却的五行分组；阴阳与不足一秒的分组不着色，数值 6 的背景没有写入者。 */
    private static int affinity(SkillBook book) {
        for (int index = 0; index < 5; index++) {
            if (book.cooldown(SkillElement.values()[index]) >= 1) {
                return index + 1;
            }
        }
        return 0;
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
