package net.yvanchuxiuzhen.system.creature.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.content.SpiritBeastBagDyeMenu;
import net.yvanchuxiuzhen.system.creature.network.SpiritBeastBagDyeButtonPayload;

/** 灵兽袋染色菜单保留来源背景、槽位和箭头按钮构图。 */
public final class SpiritBeastBagDyeScreen extends AbstractContainerScreen<SpiritBeastBagDyeMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
            "textures/screens/lingshoudaizhuanhuan.png");

    public SpiritBeastBagDyeScreen(SpiritBeastBagDyeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui." + ModId.VALUE + ".lingshoudaizhuanhuan.button_empty"), button ->
                        PacketDistributor.sendToServer(new SpiritBeastBagDyeButtonPayload(menu.containerId, 0)))
                .bounds(leftPos + 78, topPos + 33, 35, 20)
                .tooltip(Tooltip.create(Component.translatable("screen." + ModId.VALUE + ".spirit_beast_dye.action")))
                .build());
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
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) {
            minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
}
