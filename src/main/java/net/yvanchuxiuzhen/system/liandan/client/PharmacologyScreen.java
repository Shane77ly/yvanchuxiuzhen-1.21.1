package net.yvanchuxiuzhen.system.liandan.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.liandan.content.PharmacologyBlockEntity;
import net.yvanchuxiuzhen.system.liandan.content.PharmacologyMenu;
import net.yvanchuxiuzhen.system.liandan.network.PharmacologyPayload;

/** 药理台使用来源画布与右侧药性面板，输入框只保存来源客户端静态名称。 */
public final class PharmacologyScreen extends AbstractContainerScreen<PharmacologyMenu> {
    private EditBox name;

    public PharmacologyScreen(PharmacologyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        name = new EditBox(font, leftPos + 27, topPos - 21, 120, 20, text("namexieru"));
        name.setMaxLength(32767);
        name.setHint(text("namexieru"));
        name.setResponder(PharmacologyMenu::inputName);
        PharmacologyMenu.inputName("");
        addRenderableWidget(name);
        Button write = new TextureButton(leftPos + 52, topPos + 37, 70, 21,
                texture("atlas/imagebutton_xieru01"), 70, 42, button -> press(0));
        write.setMessage(Component.literal("写入丹方"));
        addRenderableWidget(write);
        Button refresh = new TextureButton(leftPos + 183, topPos + 6, 16, 16,
                texture("atlas/imagebutton_shuaxin01"), 16, 32, button -> press(1));
        refresh.setMessage(Component.literal("刷新药性"));
        addRenderableWidget(refresh);
    }

    private void press(int button) {
        PacketDistributor.sendToServer(new PharmacologyPayload(menu.containerId, button));
        menu.press(minecraft.player, button);
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
        graphics.blit(texture("yaolitai"), leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        graphics.blit(texture("xiaoguoxianshi"), leftPos + 177, topPos, 0, 0, 104, 166, 104, 166);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        PharmacologyBlockEntity table = menu.table();
        if (table != null) {
            CompoundTag state = table.getPersistentData();
            for (int index = 0; index < 13; index++) {
                graphics.drawString(font, state.getString("effectxianshi" + index), 186, 34 + index * 9, -1, false);
            }
            CompoundTag preview = table.getItem(11).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            graphics.drawString(font, preview.getString("danyaoname"), 200, 11, -13210, false);
        }
        graphics.drawString(font, text("label_dan_fang"), 150, -20, -1, false);
        graphics.drawString(font, text("label_jian_jie"), 150, -11, -1, false);
        graphics.drawString(font, text("label_dan_yao"), 168, -20, -1, false);
        graphics.drawString(font, text("label_jian_jie1"), 168, -11, -1, false);
    }

    private static Component text(String suffix) {
        return Component.translatable("gui.yvanchuxiuzhen.yaolitai." + suffix);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
