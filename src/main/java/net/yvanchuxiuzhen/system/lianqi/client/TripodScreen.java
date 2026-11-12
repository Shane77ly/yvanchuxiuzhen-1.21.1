package net.yvanchuxiuzhen.system.lianqi.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.lianqi.content.TripodMenu;
import net.yvanchuxiuzhen.system.lianqi.network.TripodButtonPayload;

/** 炼器界面保留来源画布、贴图、九条图纸摘要及九个按钮的位置和操作。 */
public final class TripodScreen extends AbstractContainerScreen<TripodMenu> {
    public TripodScreen(TripodMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        addButton(227, 177, 44, 26, "lianzhikaishi01", 0, "开始炼器");
        addButton(68, 126, 31, 18, "daochu01", 1, "按图纸装料");
        addButton(104, 126, 31, 18, "daoru01", 2, "保存图纸");
        addButton(42, 277, 18, 18, "lianqishuominganniu01", 3, "升级说明");
        addButton(42, 237, 18, 18, "lianqishuominganniu03", 4, "熔炼和精炼说明");
        addButton(42, 257, 18, 18, "lianqishuominganniu05", 5, "炼器说明");
        addButton(42, 297, 18, 18, "lianqishuominganniu07", 6, "图纸制作说明");
        addButton(239, 157, 22, 22, "cailiaodaianniu01", 7, "领取成品");
        addButton(440, 218, 12, 12, "bankgui10", 8, "鼎炉属性说明");
    }

    private void addButton(int x, int y, int width, int height, String name, int id, String message) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + name), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new TripodButtonPayload(id)));
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
        blit(graphics, "lianqi_gui", 43, 121, 414, 214);
        blit(graphics, "lianqi_gui_slot", 168, 202, 18, 18);
        for (int slot = 1; slot <= 8; slot++) {
            if (!menu.getSlot(slot - 1).getItem().isEmpty()) blit(graphics, "lianqi_gui_slot", 168 + slot * 18, 202, 18, 18);
        }
        int progress = menu.progress();
        for (int segment = 0; segment < 10; segment++) {
            if (progress >= (segment == 0 ? 1 : segment * 10)) blit(graphics, "lianqi_gui_jindu", 159 + segment * 18, 125, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        CompoundTag data = menu.data();
        for (int slot = 0; slot < 9; slot++) graphics.drawString(font, menu.recipeLine(slot), 51, 147 + slot * 9, -12829636, false);
        graphics.drawString(font, data.getString("lianqiwenzi"), 242, 224, -26368, false);
        graphics.drawString(font, menu.tripodName(), 366, 129, -13312, false);
        label(graphics, "lianqibiao01", data.getInt("lianpower"), 147, -16724788);
        label(graphics, "lianqibiao02", data.getInt("TripodLevel"), 161, -26368);
        label(graphics, "lianqibiao03", data.getInt("TripodFire"), 172, -26368);
        label(graphics, "lianqibiao04", data.getInt("TripodPurification"), 183, -26368);
    }

    private void label(GuiGraphics graphics, String key, int value, int y, int color) {
        graphics.drawString(font, Component.translatable(key).getString() + "：" + value, 366, y, color, false);
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
