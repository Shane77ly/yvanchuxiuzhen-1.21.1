package net.yvanchuxiuzhen.system.quest.client;

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
import net.yvanchuxiuzhen.system.quest.content.QuestTriggerEditorMenu;
import net.yvanchuxiuzhen.system.quest.network.QuestTriggerEditorPayload;

/** 来源单槽任务编辑界面的三列输入、十个任务类型选项与粒子开关。 */
public final class QuestTriggerEditorScreen extends AbstractContainerScreen<QuestTriggerEditorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/renwufangkuai_gui.png");
    private static final String[] FIELDS = {"renwulist", "shopinformation", "productlist"};
    private static final int[] COLUMNS = {-155, 177, 303};
    private final EditBox[][] inputs = new EditBox[3][10];
    private final Checkbox[] advanced = new Checkbox[10];
    private Checkbox particles;

    public QuestTriggerEditorScreen(QuestTriggerEditorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        for (int column = 0; column < FIELDS.length; column++) {
            for (int row = 0; row < 10; row++) {
                Component prompt = text(FIELDS[column] + (row + 1));
                EditBox input = new EditBox(font, leftPos + COLUMNS[column], topPos - 11 + row * 18, 120, 20, prompt);
                input.setMaxLength(32767);
                if (row == 0) {
                    input.setSuggestion(prompt.getString());
                    input.setResponder(value -> input.setSuggestion(value.isEmpty() ? prompt.getString() : null));
                }
                inputs[column][row] = addRenderableWidget(input);
            }
        }
        addRenderableWidget(Button.builder(text("button_set"), button -> save()).bounds(leftPos + 69, topPos + 34, 40, 20).build());
        for (int row = 0; row < 10; row++) {
            advanced[row] = addRenderableWidget(Checkbox.builder(text("gaojirenwu" + (row + 1)), font)
                    .pos(leftPos - 29, topPos - 11 + row * 18).selected(menu.advanced(minecraft.player, row + 1)).build());
        }
        particles = addRenderableWidget(Checkbox.builder(text("lizi"), font).pos(leftPos + 6, topPos + 61).selected(false).build());
    }

    private void save() {
        StringBuilder state = new StringBuilder();
        for (int column = 0; column < FIELDS.length; column++) {
            for (int row = 0; row < 10; row++) {
                state.append(FIELDS[column]).append(row + 1).append(':').append(inputs[column][row].getValue()).append(',');
            }
        }
        for (int row = 0; row < 10; row++) state.append("gaojirenwu").append(row + 1).append('-').append(advanced[row].selected() ? 1 : 0).append(',');
        state.append("lizi-").append(particles.selected() ? 1 : 0).append(',');
        PacketDistributor.sendToServer(new QuestTriggerEditorPayload(menu.containerId, state.toString()));
    }

    private static Component text(String key) {
        return Component.translatable("gui.yvanchuxiuzhen.renwufangkuai_gui." + key);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, text("label_xian_shi_ci_wu"), 24, 7, -12829636, false);
    }
}
