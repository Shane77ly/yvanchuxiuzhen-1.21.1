package net.yvanchuxiuzhen.collection.decor.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorBlockEntity;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorMenu;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorPayload;
import net.yvanchuxiuzhen.platform.ModId;

/** 原生编辑屏幕保留来源画布和控件位置，文字写入交给服务端菜单。 */
public final class InscribedDecorScreen extends AbstractContainerScreen<InscribedDecorMenu> {
    private EditBox text;
    private int ticks;
    public InscribedDecorScreen(InscribedDecorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }
    @Override protected void init() {
        super.init();
        boolean memorial = menu.kind().memorial();
        boolean legacy = menu.kind() == InscribedDecorMenu.Kind.LEGACY_PLAQUE;
        String key = "gui.yvanchuxiuzhen." + menu.kind().id() + ".";
        int x = memorial ? 24 : legacy ? 6 : 5;
        int y = memorial ? 34 : legacy ? 8 : 6;
        text = new EditBox(font, leftPos + x, topPos + y, 120, 20, Component.translatable(key + "plaquetxt"));
        text.setMaxLength(32767);
        text.setSuggestion(Component.translatable(key + "plaquetxt").getString());
        text.setResponder(value -> text.setSuggestion(value.isEmpty() ? Component.translatable(key + "plaquetxt").getString() : null));
        addRenderableWidget(text);
        addRenderableWidget(Button.builder(Component.translatable(key + (memorial ? "button_she_zhi" : "button_ok")), button -> send(0))
                .bounds(leftPos + (memorial ? 69 : 130), topPos + (memorial ? 88 : y), 35, 20).build());
        if (!memorial) {
            addRenderableWidget(Button.builder(Component.translatable(key + "button_empty"), button -> send(1))
                    .bounds(leftPos + 6, topPos + 70, 35, 20).build());
            addRenderableWidget(Button.builder(Component.translatable(key + "button_05"), button -> send(2))
                    .bounds(leftPos + 6, topPos + 97, 46, 20).build());
        }
    }
    private void send(int button) {
        String state = menu.kind() == InscribedDecorMenu.Kind.LEGACY_PLAQUE ? "" : "plaquetxt:" + text.getValue() + ",";
        PacketDistributor.sendToServer(new InscribedDecorPayload(menu.containerId, button, state));
    }
    @Override protected void containerTick() {
        super.containerTick();
        InscribedDecorBlockEntity block = menu.block();
        if (++ticks == 2 && !menu.kind().memorial() && block != null
                && !block.plaqueText().isEmpty()) text.setValue(block.plaqueText());
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + (menu.kind().memorial() ? "paiwei_gui" : "plaque_gui") + ".png");
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.kind().memorial() || menu.block() == null) return;
        InscribedDecorBlockEntity block = menu.block();
        boolean legacy = menu.kind() == InscribedDecorMenu.Kind.LEGACY_PLAQUE;
        graphics.drawString(font, block.plaqueText(), 6, 34, -3368704, false);
        graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.plaque_gui.angle", block.pitchAngle()), legacy ? 45 : 51, legacy ? 78 : 76, -12829636, false);
        graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.plaque_gui." + (block.addHalf() ? "half_on" : "half_off")),
                60, legacy ? 106 : 103, -12829636, false);
    }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) { minecraft.player.closeContainer(); return true; }
        return text.isFocused() ? text.keyPressed(key, scanCode, modifiers) : super.keyPressed(key, scanCode, modifiers);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
