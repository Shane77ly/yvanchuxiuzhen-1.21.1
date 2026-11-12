package net.yvanchuxiuzhen.system.fulu.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Locale;
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
import net.yvanchuxiuzhen.system.fulu.DrawingRecipes;
import net.yvanchuxiuzhen.system.fulu.DrawingTableRuntime;
import net.yvanchuxiuzhen.system.fulu.content.DrawingTableBlockEntity;
import net.yvanchuxiuzhen.system.fulu.content.DrawingTableMenu;
import net.yvanchuxiuzhen.system.fulu.network.DrawingTableButtonPayload;

/** 画符台保留来源画布、九个按钮和符形叠图；文字读取服务端同步的共享选择。 */
public final class DrawingTableScreen extends AbstractContainerScreen<DrawingTableMenu> {
    public DrawingTableScreen(DrawingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        addButton(105, 165, 20, 13, "huafutai_down01", 0, "previous_head");
        addButton(105, 228, 20, 13, "huafutai_down011", 1, "previous_pillar");
        addButton(105, 291, 20, 13, "huafutai_down012", 2, "previous_core");
        addButton(204, 165, 20, 13, "huafutai_up01", 3, "next_head");
        addButton(204, 228, 20, 13, "huafutai_up011", 4, "next_pillar");
        addButton(204, 291, 20, 13, "huafutai_up012", 5, "next_core");
        addButton(314, 204, 14, 17, "huafutaianniu01", 6, "draw");
        addButton(78, 138, 12, 12, "bankgui10", 7, "usage");
        addButton(78, 156, 12, 12, "bankgui101", 8, "materials");
    }

    private void addButton(int x, int y, int width, int height, String texture, int operation, String label) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + texture), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new DrawingTableButtonPayload(menu.containerId, operation)));
        button.setMessage(text(label));
        addRenderableWidget(button);
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
        drawTexture(graphics, "huafutaiui", 89, 130, 320, 240);
        drawTexture(graphics, "lianqi_gui_slot", 312, 174, 18, 18);
        DrawingTableBlockEntity table = menu.table();
        if (table != null) {
            CompoundTag state = table.getPersistentData();
            drawSelection(graphics, "futou", state.getInt("futou"), 11, 135, 135, 58, 58);
            drawSelection(graphics, "chiling", state.getInt("tiandizhu"), 4, 135, 194, 58, 168);
            drawSelection(graphics, "fudan", state.getInt("fudan"), 8, 152, 255, 24, 104);
        }
        RenderSystem.disableBlend();
    }

    private void drawSelection(GuiGraphics graphics, String prefix, int selected, int count, int x, int y, int width, int height) {
        if (selected >= 1 && selected <= count) drawTexture(graphics, prefix + String.format(Locale.ROOT, "%02d", selected), x, y, width, height);
    }

    private void drawTexture(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        DrawingTableBlockEntity table = menu.table();
        CompoundTag state = table == null ? new CompoundTag() : table.getPersistentData();
        graphics.drawString(font, text("head", selection(state, "futou")), 249, 138, -1, false);
        graphics.drawString(font, text("pillar", selection(state, "tiandizhu")), 249, 147, -1, false);
        graphics.drawString(font, text("core", selection(state, "fudan")), 249, 156, -1, false);
        Component preview = state.getString("fuluname").isEmpty() ? text("none") : text("preview",
                DrawingRecipes.name(ResourceLocation.parse(state.getString("fuluname"))), selection(state, "tiandizhu"), state.getInt("needmotiao"));
        graphics.drawString(font, preview, 249, 129, -12829636, false);
    }

    private static Component selection(CompoundTag state, String category) {
        int selected = state.getInt(category);
        return selected == 0 ? Component.empty() : Component.translatable(DrawingTableRuntime.knowledgeKey(category, selected));
    }

    private static Component text(String suffix, Object... arguments) {
        return Component.translatable("gui.yvanchuxiuzhen.huafu_gui." + suffix, arguments);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
