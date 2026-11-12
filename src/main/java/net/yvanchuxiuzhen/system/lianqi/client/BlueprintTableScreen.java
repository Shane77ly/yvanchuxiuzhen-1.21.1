package net.yvanchuxiuzhen.system.lianqi.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.lianqi.content.BlueprintTableMenu;
import net.yvanchuxiuzhen.system.lianqi.network.BlueprintTableButtonPayload;

/** 天道图纸台保留来源画布、实际材料目录、十三行预览及本进程名称输入。 */
public final class BlueprintTableScreen extends AbstractContainerScreen<BlueprintTableMenu> {
    private EditBox name;

    public BlueprintTableScreen(BlueprintTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 480;
        imageHeight = 280;
    }

    @Override
    protected void init() {
        super.init();
        name = new EditBox(font, leftPos + 293, topPos + 1, 120, 20, text("lianqiname"));
        name.setMaxLength(32767);
        name.setHint(text("lianqiname"));
        name.setResponder(BlueprintTableMenu::inputName);
        BlueprintTableMenu.inputName("");
        addRenderableWidget(name);
        addButton(437, 127, 16, 16, "shuaxin01", 0, "刷新图纸预览");
        addButton(338, 163, 31, 18, "daoru01", 1, "保存图纸");
    }

    private void addButton(int x, int y, int width, int height, String texture, int operation, String label) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + texture), width, height * 2, pressed -> {
                    PacketDistributor.sendToServer(new BlueprintTableButtonPayload(menu.containerId, operation));
                    menu.press(minecraft.player, operation);
                });
        button.setMessage(Component.literal(label));
        addRenderableWidget(button);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) {
            minecraft.player.closeContainer();
            return true;
        }
        return name.isFocused() ? name.keyPressed(key, scanCode, modifiers) : super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture("tiandaotuzhitai_gui"), leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, text("label_zhuang_bei"), 293, 163, -3368704, false);
        graphics.drawString(font, text("label_jian_jie"), 311, 163, -13408768, false);
        graphics.drawString(font, text("label_yu_jian"), 374, 163, -16751002, false);
        graphics.drawString(font, text("label_diu_qi"), 437, 163, -10092493, false);
        if (menu.table() == null) return;
        CompoundTag data = menu.table().getPersistentData().getCompound("zong");
        boolean armor = Set.of("头盔", "胸甲", "裤子", "战靴").contains(data.getString("fabaotype"));
        String support = armor ? "§b" : "§4";
        String attack = armor ? "§4" : "§b";
        String[] lines = {
            support + stat(data, "血量上限", "health_Max", "heanum") + " " + stat(data, "血量恢复", "health_Rec", "hearecnum"),
            support + stat(data, "灵力上限", "power_Max", "powernum") + " " + stat(data, "灵力恢复", "power_Rec", "powerrecnum"),
            support + stat(data, "精力上限", "soul_Max", "jingnum") + " " + stat(data, "精力恢复", "soul_Rec", "jingrecnum"),
            attack + stat(data, "物理攻击", "attack_num", "attnum") + support + stat(data, "物理防御", "attack_Def", "attdefnum"),
            attack + stat(data, "法术攻击", "mana_Att", "mananum") + support + stat(data, "法术防御", "mana_Def", "manadefnum"),
            attack + stat(data, "精神攻击", "soul_Att", "soulnum") + support + stat(data, "精神防御", "soul_Def", "souldefnum"),
            (data.getInt("weightnum") > 100 ? "§4" : "§b") + "重量：" + data.getInt("weightnum") + "§b" + percent(data, "击退抗性", "knockback_resistance", "knockbacknum"),
            "§b" + percent(data, "移动速度", "speed_num", "spenum") + "§b" + percent(data, "攻击速度", "attack_speed", "attspeednum")
        };
        for (int row = 0; row < lines.length; row++) graphics.drawString(font, lines[row], 293, 37 + row * 9, -16777216, false);
        graphics.drawString(font, "§6" + data.getString("item_level") + "（" + data.getInt("item_level_num") + "）" + data.getString("fabaotype"), 293, 28, -3368704, false);
        for (int row = 0; row < 4; row++) {
            graphics.drawString(font, enchantment(data, row * 2) + enchantment(data, row * 2 + 1), 293, 109 + row * 9, -16777216, false);
        }
    }

    private static String stat(CompoundTag data, String name, String value, String raw) {
        return name + "+" + data.getInt(value) + "（" + data.getInt(raw) + "）";
    }

    private static String percent(CompoundTag data, String name, String value, String raw) {
        return name + "+" + new DecimalFormat("0").format(data.getDouble(value) * 100) + "%（" + data.getDouble(raw) + "）";
    }

    private static String enchantment(CompoundTag data, int index) {
        String prefix = "fumo" + index;
        if (data.getString(prefix).isEmpty()) return "";
        String namespace = data.getString(prefix + "kongjian");
        if (namespace.equals("yvanchuxitong")) namespace = ModId.VALUE;
        return "§b" + Component.translatable("enchantment." + namespace + "." + data.getString(prefix)).getString()
                + "（LV" + data.getInt(prefix + "num") / 100 + "）(" + data.getInt(prefix + "num") + ")";
    }

    private static Component text(String suffix) {
        return Component.translatable("gui.yvanchuxiuzhen.tiandaotuzhitai_gui." + suffix);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
