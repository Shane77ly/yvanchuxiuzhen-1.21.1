package net.yvanchuxiuzhen.system.economy.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;
import net.yvanchuxiuzhen.system.economy.api.BankDisplay;
import net.yvanchuxiuzhen.system.economy.content.BankMenu;
import net.yvanchuxiuzhen.system.economy.network.BankButtonPayload;

/** 来源500画布、九级取款按钮与存款入口；余额文字只读服务端的本人显示投影。 */
public final class BankScreen extends AbstractContainerScreen<BankMenu> {
    private final Player player;

    public BankScreen(BankMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        player = inventory.player;
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        for (int tier = 1; tier <= 9; tier++) {
            String texture = tier == 1 ? "bankgui02" : "bankgui02" + (tier - 1);
            addButton(129 + (tier - 1) % 3 * 81, 180 + (tier - 1) / 3 * 27, 79, 24, texture, tier - 1,
                    text("withdraw", Component.translatable("item.yvanchuxiuzhen.cailiao_19" + tier)));
        }
        addButton(91, 155, 20, 20, "bankgui05", 9, text("deposit"));
        addButton(357, 156, 12, 12, "bankgui12", 10, text("close"));
        addButton(339, 156, 12, 12, "bankgui10", 11, text("explain"));
    }

    private void addButton(int x, int y, int width, int height, String image, int operation, Component label) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + image), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new BankButtonPayload(menu.containerId, operation)));
        button.setMessage(label);
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
        drawTexture(graphics, "wupinlan", 162, 255, 176, 94);
        drawTexture(graphics, "bankgui01", 111, 113, 276, 151);
        drawTexture(graphics, "bankgui04", 63, 150, 52, 30);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, text("title"), 233, 131, -154, false);
        BankDisplay display = BankAccounts.display(player);
        if (!display.ready()) return;
        for (int tier = 1; tier <= 9; tier++) {
            int amount = display.amounts().balance(tier);
            Component label = amount >= 1 ? text("amount", Integer.toString(amount)) : text("none");
            graphics.drawString(font, label, 155 + (tier - 1) % 3 * 81, 183 + (tier - 1) / 3 * 27, -1, false);
        }
        graphics.drawString(font, display.master(), 159, 156, -13210, false);
        graphics.drawString(font, text("purchasing_power", new DecimalFormat("0").format(display.amounts().purchasingPower())), 159, 165, -1, false);
    }

    private void drawTexture(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static Component text(String suffix, Object... arguments) {
        return Component.translatable("gui.yvanchuxiuzhen.bank_gui." + suffix, arguments);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
