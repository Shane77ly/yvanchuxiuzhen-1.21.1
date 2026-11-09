package net.yvanchuxiuzhen.system.cooking.client;

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
import net.yvanchuxiuzhen.system.cooking.CookingRecipes;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotMenu;
import net.yvanchuxiuzhen.system.cooking.network.CookingButtonPayload;

/** 两锅使用来源画布、食材和容器布局；动态物品名称由当前客户端语言解析。 */
public final class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    public CookingPotScreen(CookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        addButton(240, menu.iron() ? 211 : 216, 18, 27, "pengrenbegin01", 0, "开始烹饪");
        addButton(108, 246, 12, 12, "bankgui10", 1, "使用说明");
        addButton(108, menu.iron() ? 260 : 261, 12, 12, "bankgui101", 2, "墨料坯说明");
    }

    private void addButton(int x, int y, int width, int height, String texture, int operation, String label) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + texture), width, height * 2, pressed -> {
                    if (operation != 1 || menu.iron()) PacketDistributor.sendToServer(new CookingButtonPayload(operation));
                });
        button.setMessage(Component.literal(label));
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
        blit(graphics, menu.iron() ? "tieguo" : "shaguo", menu.iron() ? 142 : 141, 100, 216, 233);
        for (int y : new int[] {250, 268, 286, 308}) blit(graphics, "chujvwupinlan", 169, y, 162, 18);
        blit(graphics, "caidangui", 352, 186, 110, 148);
        blit(graphics, "chengfangui", 124, 246, 34, 88);
        blit(graphics, "lianqi_gui_slot", 438, 192, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        CompoundTag recipe = CookingRecipes.data(menu.recipe());
        graphics.drawString(font, recipe.getString("MenuName"), 362, 192, -26368, false);
        String container = recipe.getString("needcontainer");
        Component containerName = container.isEmpty() || container.equals("无")
                ? Component.literal("不需要盛装菜品的容器") : CookingRecipes.stack(container).getDisplayName();
        graphics.drawString(font, containerName, 357, 211, -16724788, false);
        for (int ingredient = 0; ingredient < 9; ingredient++) {
            String identity = recipe.getString("cailiao0" + ingredient);
            if (!identity.isEmpty()) graphics.drawString(font, CookingRecipes.stack(identity).getDisplayName(),
                    357, 247 + ingredient * 9, -16777216, false);
        }
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
