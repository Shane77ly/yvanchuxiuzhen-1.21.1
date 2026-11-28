package net.yvanchuxiuzhen.system.treasure.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.treasure.TreasureToolSettings;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.content.TreasureToolMenu;
import net.yvanchuxiuzhen.system.treasure.network.TreasureToolButtonPayload;

/** 来源五行管理页保留背景、十个箭头坐标和只在客户端发生的显示默认值写入。 */
public final class TreasureToolScreen extends AbstractContainerScreen<TreasureToolMenu> {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");
    private static final int COLOR = -10079488;

    public TreasureToolScreen(TreasureToolMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int[] x = {24, 141, 24, 141, 24, 141, 15, 69, 96, 150};
        int[] y = {25, 25, 61, 61, 97, 97, 133, 133, 133, 133};
        for (int button = 0; button < x.length; button++) {
            int action = button;
            TextureButton widget = new TextureButton(leftPos + x[button], topPos + y[button], 8, 10,
                    texture("atlas/imagebutton_gui0401" + (button % 2 == 0 ? "down01" : "up01")), 8, 20, ignored -> {
                        PacketDistributor.sendToServer(new TreasureToolButtonPayload(menu.containerId, action));
                        menu.clickMenuButton(minecraft.player, action);
                    });
            widget.setMessage(text(button % 2 == 0 ? "previous" : "next"));
            addRenderableWidget(widget);
        }
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + path + ".png");
    }

    private static Component text(String key) {
        return Component.translatable("screen.yvanchuxiuzhen.treasure_tool." + key);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture("tiandaobaoxiang"), leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, text("category"), 6, 7, COLOR, false);
        graphics.drawString(font, text("subtype"), 6, 43, COLOR, false);
        graphics.drawString(font, text("level"), 6, 79, COLOR, false);
        graphics.drawString(font, text("maximum"), 123, 115, COLOR, false);
        graphics.drawString(font, text("minimum"), 6, 115, COLOR, false);
        TreasureContainerConfig data = TreasureToolSettings.display(minecraft.player.getMainHandItem());
        graphics.drawString(font, data.category(), 51, 25, COLOR, false);
        graphics.drawString(font, data.subtype(), 51, 61, COLOR, false);
        graphics.drawString(font, text("grade").copy().append(WHOLE.format(data.level())), 51, 97, COLOR, false);
        graphics.drawString(font, text("min").copy().append(WHOLE.format(data.minimum())), 33, 133, COLOR, false);
        graphics.drawString(font, text("max").copy().append(WHOLE.format(data.maximum())), 114, 133, COLOR, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) {
            minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
}
