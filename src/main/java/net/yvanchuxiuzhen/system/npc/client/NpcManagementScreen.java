package net.yvanchuxiuzhen.system.npc.client;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.system.npc.NpcManagementData;
import net.yvanchuxiuzhen.system.npc.NpcManagementMenu;
import net.yvanchuxiuzhen.system.npc.network.NpcManagementPayload;
import org.lwjgl.glfw.GLFW;

/** 天道NPC设置工具编辑界面，字段通过服务端载荷写回手持工具。 */
public final class NpcManagementScreen extends AbstractContainerScreen<NpcManagementMenu> {
    private static final int BACKGROUND = 0xD0101010;
    private static final int CONTENT_BACKGROUND = 0xB0202020;
    private static final int BORDER = 0xFF666666;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int SLOT_BACKGROUND = 0xFF373737;
    private static final int SLOT_LIGHT_EDGE = 0xFF8A8A8A;
    private static final int SLOT_DARK_EDGE = 0xFF171717;
    private static final List<String> RATINGS = List.of("healthmaxtxt", "healthrectxt", "powermaxtxt", "powerrectxt",
            "jinglimaxtxt", "jinglirectxt", "attacktxt", "defensetxt", "manaatttxt", "manadeftxt", "soulatttxt",
            "souldeftxt", "speednumtxt");
    private static final List<String> RATING_VALUES = List.of("贫弱", "勉强", "平庸", "强劲", "绝佳");

    private final Map<String, String> values = new LinkedHashMap<>();
    private final Map<String, EditBox> visibleFields = new LinkedHashMap<>();
    private final List<FieldLabel> labels = new ArrayList<>();
    private Page page = Page.PROFILE;
    private int selectedRating;
    private boolean fixed;

    public NpcManagementScreen(NpcManagementMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 420;
        imageHeight = 250;
        for (String key : NpcManagementData.KEYS) values.put(key, menu.fieldValue(key));
    }

    @Override
    protected void init() {
        super.init();
        buildWidgets();
    }

    private void buildWidgets() {
        visibleFields.clear();
        labels.clear();
        addTab(Page.PROFILE, 8);
        addTab(Page.TASKS, 82);
        addTab(Page.SHOP, 156);
        addTab(Page.RATINGS, 230);
        addRenderableWidget(Button.builder(Component.translatable("gui.yvanchuxiuzhen.npc.management.save"), button -> save())
                .bounds(leftPos + 338, topPos + 4, 74, 18).build());
        switch (page) {
            case PROFILE -> buildProfileFields();
            case TASKS -> buildTaskFields();
            case SHOP -> buildShopFields();
            case RATINGS -> buildRatingFields();
        }
        if (!visibleFields.isEmpty()) setInitialFocus(visibleFields.values().iterator().next());
    }

    private void addTab(Page tab, int x) {
        Component title = Component.translatable("gui.yvanchuxiuzhen.npc.management.tab." + tab.key);
        Button button = Button.builder(title, pressed -> selectPage(tab)).bounds(leftPos + x, topPos + 24, 70, 18).build();
        button.active = tab != page;
        addRenderableWidget(button);
    }

    private void selectPage(Page next) {
        rememberVisibleValues();
        page = next;
        rebuildWidgets();
    }

    protected void rebuildWidgets() {
        clearWidgets();
        setFocused(null);
        buildWidgets();
    }

    private void buildProfileFields() {
        addField("npcname", 8, 58, 54, 342);
        addRenderableWidget(Button.builder(Component.translatable("gui.yvanchuxiuzhen.npc.management.level.down"), button -> changeLevel(-1))
                .bounds(leftPos + 8, topPos + 87, 80, 18).build());
        labels.add(new FieldLabel(Component.translatable("gui.yvanchuxiuzhen.npc.management.level.value",
                new DecimalFormat("0").format(Double.parseDouble(values.get("npclevel")))), 94, 92));
        addRenderableWidget(Button.builder(Component.translatable("gui.yvanchuxiuzhen.npc.management.level.up"), button -> changeLevel(1))
                .bounds(leftPos + 158, topPos + 87, 80, 18).build());
        addRenderableWidget(Checkbox.builder(Component.translatable("gui.yvanchuxiuzhen.npc.management.fixed"), font)
                .pos(leftPos + 260, topPos + 87).selected(fixed)
                .onValueChange((checkbox, selected) -> fixed = selected).build());
    }

    private void changeLevel(int amount) {
        rememberVisibleValues();
        double level = Double.parseDouble(values.get("npclevel")) + amount;
        if (level < 0) level = 41;
        if (level > 41) level = 0;
        values.put("npclevel", Double.toString(level));
        rebuildWidgets();
    }

    private void buildTaskFields() {
        for (int index = 1; index <= 10; index++) {
            int column = index <= 5 ? 0 : 1;
            int row = (index - 1) % 5;
            int x = column == 0 ? 8 : 212;
            addField("renwulist" + index, x, 45 + row * 19, 44, 154);
        }
    }

    private void buildShopFields() {
        addField("productlist", 8, 58, 62, 334);
        addField("shopinformation", 8, 89, 62, 334);
    }

    private void buildRatingFields() {
        String key = RATINGS.get(selectedRating);
        Component attribute = Component.translatable("gui.yvanchuxiuzhen.npc.management.field." + key);
        int rating = RATING_VALUES.indexOf(values.get(key));
        Component potential = Component.translatable("gui.yvanchuxiuzhen.npc.management.rating." + (rating + 1));
        labels.add(new FieldLabel(Component.translatable("gui.yvanchuxiuzhen.npc.management.rating.current", attribute, potential), 82, 61));
        addRenderableWidget(Button.builder(Component.literal("<<"), button -> selectRating(-1))
                .bounds(leftPos + 8, topPos + 55, 60, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.yvanchuxiuzhen.npc.management.rating.previous"))).build());
        addRenderableWidget(Button.builder(Component.literal(">>"), button -> selectRating(1))
                .bounds(leftPos + 352, topPos + 55, 60, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.yvanchuxiuzhen.npc.management.rating.next"))).build());
        for (int index = 0; index < RATING_VALUES.size(); index++) {
            String value = RATING_VALUES.get(index);
            Button choice = Button.builder(Component.translatable("gui.yvanchuxiuzhen.npc.management.rating." + (index + 1)), button -> {
                values.put(key, value);
                rebuildWidgets();
            }).bounds(leftPos + 8 + index * 82, topPos + 89, 76, 20).build();
            choice.active = index != rating;
            addRenderableWidget(choice);
        }
    }

    private void selectRating(int direction) {
        selectedRating = Math.floorMod(selectedRating + direction, RATINGS.size());
        rebuildWidgets();
    }

    private void addField(String key, int x, int y, int labelWidth, int fieldWidth) {
        Component label = Component.translatable("gui.yvanchuxiuzhen.npc.management.field." + key);
        labels.add(new FieldLabel(label, x, y + 3));
        EditBox field = new EditBox(font, leftPos + x + labelWidth, topPos + y, fieldWidth, 14, label);
        field.setMaxLength(32767);
        field.setValue(values.getOrDefault(key, ""));
        field.setResponder(value -> values.put(key, value));
        visibleFields.put(key, field);
        addRenderableWidget(field);
    }

    private void rememberVisibleValues() {
        visibleFields.forEach((key, field) -> values.put(key, field.getValue()));
    }

    private void save() {
        rememberVisibleValues();
        List<String> payload = NpcManagementData.KEYS.stream().map(values::get).toList();
        PacketDistributor.sendToServer(new NpcManagementPayload(payload, fixed));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != GLFW.GLFW_KEY_ESCAPE && keyCode != GLFW.GLFW_KEY_TAB && getFocused() instanceof EditBox field) {
            field.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, BACKGROUND);
        graphics.fill(leftPos + 4, topPos + 42, leftPos + imageWidth - 4, topPos + 145, CONTENT_BACKGROUND);
        graphics.fill(leftPos + 4, topPos + 42, leftPos + imageWidth - 4, topPos + 43, BORDER);
        for (int index = 0; index < menu.slots.size(); index++) {
            Slot slot = menu.slots.get(index);
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            graphics.fill(x, y, x + 18, y + 18, SLOT_BACKGROUND);
            graphics.fill(x, y, x + 18, y + 1, SLOT_LIGHT_EDGE);
            graphics.fill(x, y, x + 1, y + 18, SLOT_LIGHT_EDGE);
            graphics.fill(x + 17, y, x + 18, y + 18, SLOT_DARK_EDGE);
            graphics.fill(x, y + 17, x + 18, y + 18, SLOT_DARK_EDGE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, TEXT, false);
        graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.npc.management.equipment"), 8, 153, TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, 175, TEXT, false);
        for (FieldLabel label : labels) graphics.drawString(font, label.text, label.x, label.y, TEXT, false);
    }

    private enum Page {
        PROFILE("profile"), TASKS("tasks"), SHOP("shop"), RATINGS("ratings");

        private final String key;

        Page(String key) { this.key = key; }
    }

    private record FieldLabel(Component text, int x, int y) { }
}
