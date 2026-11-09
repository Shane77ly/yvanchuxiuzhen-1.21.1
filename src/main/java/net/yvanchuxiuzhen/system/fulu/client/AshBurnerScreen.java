package net.yvanchuxiuzhen.system.fulu.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.fulu.content.AshBurnerMenu;
import net.yvanchuxiuzhen.system.fulu.network.AshBurnerHelpPayload;

/** 烧灰架沿用九槽画布与帮助图标，操作仅提交服务端。 */
public final class AshBurnerScreen extends AbstractContainerScreen<AshBurnerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/huafushaohui_gui.png");

    public AshBurnerScreen(AshBurnerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        Button help = new TextureButton(leftPos + 40, topPos + 36, 12, 12,
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/atlas/imagebutton_bankgui10.png"), 12, 24,
                button -> PacketDistributor.sendToServer(new AshBurnerHelpPayload(menu.containerId)));
        help.setMessage(Component.literal("烧灰架使用指南"));
        addRenderableWidget(help);
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
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
