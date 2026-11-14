package net.yvanchuxiuzhen.system.quest.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenu;

/** 来源500×500无背景对话；八行正文每行18个字符，选项保持原页面位置。 */
public final class DialogueScreen extends AbstractContainerScreen<DialogueMenu> {
    private final TextureButton[] optionButtons = new TextureButton[4];

    public DialogueScreen(DialogueMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        if (menu.choices) {
            for (int i = 0; i < 4; i++) {
                final int button = i;
                TextureButton option = new TextureButton(leftPos + 160, topPos + 265 + i * 18, 16, 16,
                        texture("atlas/imagebutton_xvanxiang01" + (i == 0 ? "" : i)), 16, 32,
                        ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button));
                option.setMessage(Component.literal(DialogueActions.snapshot(minecraft.player).getString("daantxt" + (i + 1))));
                optionButtons[i] = option;
                addRenderableWidget(option);
            }
        } else {
            TextureButton next = new TextureButton(leftPos + 240, topPos + 300, 16, 16,
                    texture("atlas/imagebutton_jixv01"), 16, 32,
                    ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0));
            next.setMessage(Component.literal("继续对话"));
            addRenderableWidget(next);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (menu.choices) {
            CompoundTag state = DialogueActions.snapshot(minecraft.player);
            for (int i = 0; i < optionButtons.length; i++) {
                optionButtons[i].setMessage(Component.literal(state.getString("daantxt" + (i + 1))));
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (menu.choices) graphics.blit(texture("fengexian"), leftPos + 121, topPos + 256, 0, 0, 256, 2, 256, 2);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        CompoundTag state = DialogueActions.snapshot(minecraft.player);
        String text = state.getString("TXTxianshi");
        for (int line = 0; line < 8 && line * 18 < text.length(); line++) {
            graphics.drawString(font, text.substring(line * 18, Math.min((line + 1) * 18, text.length())), 168, 174 + line * 9, 0xFFFFFF, false);
        }
        if (menu.choices) {
            for (int i = 1; i <= 4; i++) graphics.drawString(font, state.getString("daantxt" + i), 179, 267 + (i - 1) * 18, 0x6699FF, false);
        }
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
