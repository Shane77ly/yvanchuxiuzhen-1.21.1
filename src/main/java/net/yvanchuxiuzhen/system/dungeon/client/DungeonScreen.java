package net.yvanchuxiuzhen.system.dungeon.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.dungeon.DungeonSettings;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonMenu;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonMenuSnapshot;
import net.yvanchuxiuzhen.system.dungeon.network.DungeonActionPayload;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 副本四页保留来源贴图、输入和槽坐标；配置值由强类型方块快照展示，按钮只提交输入。 */
public final class DungeonScreen extends AbstractContainerScreen<DungeonMenu> {
    private static final List<String> BACKGROUNDS = List.of("fubenopen", "tiandaofuben_gui", "fubenblock_gui", "fubenentity_gui");
    private static final DecimalFormat WHOLE = new DecimalFormat("0");
    private final List<EditBox> inputs = new ArrayList<>();
    private Checkbox attack;

    public DungeonScreen(DungeonMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = switch (menu.kind()) { case DungeonMenu.OPEN -> 222; case DungeonMenu.MANAGE -> 242; case DungeonMenu.CONTROLLER -> 235; default -> 166; };
    }

    @Override
    protected void init() {
        super.init();
        inputs.clear();
        if (menu.kind() == DungeonMenu.OPEN) {
            addRenderableWidget(new TextureButton(leftPos + 173, topPos + 42, 28, 12, texture("atlas/imagebutton_fubenguijinru01"), 28, 24, ignored -> send()));
        } else if (menu.kind() == DungeonMenu.MANAGE) {
            int[] x = {6, 6, 6, 6, 177, 177, 177, 177, 177, 177};
            int[] y = {18, 54, 90, 126, 135, 18, 36, 72, 108, 153};
            for (int index = 0; index < DungeonSettings.FIELDS.size(); index++) input(x[index], y[index], DungeonSettings.FIELDS.get(index));
            button(213, 207);
        } else if (menu.kind() == DungeonMenu.CONTROLLER) {
            input(51, 14, "fubenbianchang");
            button(69, 131);
        } else {
            input(51, 25, "zhanweifuid");
            attack = addRenderableWidget(Checkbox.builder(text("attack"), font).pos(leftPos + 6, topPos + 52).selected(false).build());
            button(69, 124);
        }
    }

    private void input(int x, int y, String key) {
        EditBox box = new EditBox(font, leftPos + x, topPos + y, 120, 20, text(key));
        box.setMaxLength(32767);
        box.setHint(text(key));
        inputs.add(addRenderableWidget(box));
    }

    private void button(int x, int y) { addRenderableWidget(Button.builder(text("set"), ignored -> send()).bounds(leftPos + x, topPos + y, 40, 20).build()); }
    private static Component text(String key) { return Component.translatable("screen.yvanchuxiuzhen.dungeon." + key); }
    private static ResourceLocation texture(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + path + ".png"); }

    private void send() {
        List<String> values = inputs.stream().map(EditBox::getValue).toList();
        PacketDistributor.sendToServer(new DungeonActionPayload(menu.containerId, values, attack != null && attack.selected()));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture(BACKGROUNDS.get(menu.kind())), leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        if (menu.kind() == DungeonMenu.OPEN) {
            graphics.blit(texture("fubengui"), leftPos - 49, topPos - 17, 0, 0, 272, 162, 272, 162);
            double preview = menu.snapshot().preview();
            for (int image = 1; image <= 5; image++) {
                if (preview == image) graphics.blit(texture("fubenguitupian0" + image), leftPos + 163, topPos + 55, 0, 0, 45, 71, 45, 71);
            }
        }
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        DungeonMenuSnapshot snapshot = menu.snapshot();
        if (menu.kind() == DungeonMenu.OPEN) {
            graphics.drawString(font, snapshot.dungeonName(), -31, 2, -13312, false);
            int[] lines = {16, 26, 37, 48};
            String[] textLines = {snapshot.text01(), snapshot.text02(), snapshot.text03(), snapshot.text04()};
            for (int line = 0; line < 4; line++) graphics.drawString(font, textLines[line], -31, lines[line], -12829636, false);
            double level = Cultivation.get(minecraft.player, CultivationValue.LEVEL_NUMBER);
            String color = level >= snapshot.minLevel() && level <= snapshot.maxLevel() ? "§a" : "§4";
            graphics.drawString(font, color + "Lv：" + WHOLE.format(snapshot.minLevel()) + "~" + WHOLE.format(snapshot.maxLevel()), -31, 63, -12829636, false);
            if (menu.slots.getFirst().hasItem()) graphics.drawString(font, text("ticket"), -31, 82, -12829636, false);
            graphics.drawString(font, text("people").copy().append(WHOLE.format(snapshot.maxPeople())), -32, 95, -16764007, false);
            graphics.drawString(font, text("reward").copy().append(snapshot.rewardSummary()), -32, 106, -13312, false);
        } else if (menu.kind() == DungeonMenu.MANAGE) {
            graphics.drawString(font, snapshot.dungeonName(), 6, 9, 0xFF000000, false);
            graphics.drawString(font, text("current_id").copy().append(snapshot.templateId()), 6, 45, 0xFF000000, false);
            graphics.drawString(font, snapshot.description(), 6, 81, 0xFF000000, false);
            graphics.drawString(font, text("reward").copy().append(snapshot.rewardSummary()), 6, 117, 0xFF000000, false);
            double level = Cultivation.get(minecraft.player, CultivationValue.LEVEL_NUMBER);
            String color = level >= snapshot.minLevel() && level <= snapshot.maxLevel() ? "§a" : "§4";
            graphics.drawString(font, color + "Lv：" + WHOLE.format(snapshot.minLevel()) + "~" + WHOLE.format(snapshot.maxLevel()), 177, 9, -1, false);
            graphics.drawString(font, text("people").copy().append(WHOLE.format(snapshot.maxPeople())), 177, 63, -1, false);
            graphics.drawString(font, text("preview").copy().append(WHOLE.format(snapshot.preview())), 177, 99, -1, false);
            graphics.drawString(font, text("cost"), 177, 180, -1, false);
        } else if (menu.kind() == DungeonMenu.CONTROLLER) {
            graphics.drawString(font, text("fubenbianchang").copy().append("：" + snapshot.sideLength()), 6, 5, 0x404040, false);
        } else {
            graphics.drawString(font, text("zhanweifuid").copy().append("：" + snapshot.markerId()), 6, 11, 0x404040, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) { minecraft.player.closeContainer(); return true; }
        for (EditBox input : inputs) if (input.isFocused()) return input.keyPressed(key, scanCode, modifiers);
        return super.keyPressed(key, scanCode, modifiers);
    }
}
