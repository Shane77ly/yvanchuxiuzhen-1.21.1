package net.yvanchuxiuzhen.collection.tiandao.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.collection.tiandao.NbtEditorMenu;
import net.yvanchuxiuzhen.collection.tiandao.NbtEditorPayload;
import net.yvanchuxiuzhen.platform.ModId;
import org.lwjgl.glfw.GLFW;

/** 来源176×166画布、两输入框、左侧八类型复选框与确认按钮。 */
public final class NbtEditorScreen extends AbstractContainerScreen<NbtEditorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/tiandaoitemnbt_gui.png");
    private static final String[] TYPES = {"aaa", "bbb", "ccc", "ddd", "eee", "fff", "ggg", "hhh"};
    private final Checkbox[] choices = new Checkbox[8];
    private EditBox name;
    private EditBox content;

    public NbtEditorScreen(NbtEditorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        name = input("nbtname", 4);
        content = input("nbtcontent", 24);
        addRenderableWidget(Button.builder(text("button_confirm"), button -> save()).bounds(leftPos + 128, topPos + 46, 40, 20).build());
        for (int index = 0; index < TYPES.length; index++) {
            choices[index] = addRenderableWidget(Checkbox.builder(text(TYPES[index]), font)
                    .pos(leftPos - 64, topPos + 1 + index * 21).selected(false).build());
        }
    }

    private EditBox input(String key, int y) {
        Component prompt = text(key);
        EditBox input = new EditBox(font, leftPos + 3, topPos + y, 120, 20, prompt);
        input.setMaxLength(32767);
        input.setSuggestion(prompt.getString());
        input.setResponder(value -> input.setSuggestion(value.isEmpty() ? prompt.getString() : null));
        return addRenderableWidget(input);
    }

    private void save() {
        StringBuilder state = new StringBuilder("nbtname:").append(name.getValue()).append(",nbtcontent:").append(content.getValue()).append(',');
        for (int index = 0; index < TYPES.length; index++) state.append(TYPES[index]).append('-').append(choices[index].selected() ? 1 : 0).append(',');
        PacketDistributor.sendToServer(new NbtEditorPayload(menu.containerId, state.toString()));
    }

    private static Component text(String key) {
        return Component.translatable("gui.yvanchuxiuzhen.tiandaoitemnbt_gui." + key);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeContainer();
            return true;
        }
        if (name.isFocused()) return name.keyPressed(key, scanCode, modifiers);
        if (content.isFocused()) return content.keyPressed(key, scanCode, modifiers);
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
