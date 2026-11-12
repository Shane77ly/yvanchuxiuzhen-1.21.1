package net.yvanchuxiuzhen.system.cooking.client;

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
import net.yvanchuxiuzhen.system.cooking.CookingRecipes;
import net.yvanchuxiuzhen.system.cooking.content.SteamerMenu;
import net.yvanchuxiuzhen.system.cooking.network.SteamerButtonPayload;

/** 两套来源画布与槽位布局，蒸锅动态食材名由当前客户端物品语言生成。 */
public final class SteamerScreen extends AbstractContainerScreen<SteamerMenu> {
    public SteamerScreen(SteamerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = menu.tray() ? 176 : 500;
        imageHeight = menu.tray() ? 166 : 500;
    }

    @Override
    protected void init() {
        super.init();
        if (!menu.tray()) {
            button(240, 215, 18, 27, "pengrenbegin01", 0, "start");
            button(108, 246, 12, 12, "bankgui10", 1, "help");
        }
    }

    private void button(int x, int y, int width, int height, String texture, int action, String key) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + texture), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new SteamerButtonPayload(menu.containerId, action)));
        button.setMessage(Component.translatable("gui.yvanchuxiuzhen.cooking.steamer." + key));
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
        if (menu.tray()) {
            blit(graphics, "longtiui", -3, 5, 180, 77);
            for (int y : new int[] {83, 101, 119, 141}) blit(graphics, "shipintaigui05", 6, y, 162, 18);
        } else {
            blit(graphics, "zhengguo", 141, 100, 216, 233);
            for (int y : new int[] {250, 268, 286, 308}) blit(graphics, "chujvwupinlan", 169, y, 162, 18);
            blit(graphics, "chengfangui", 124, 246, 34, 88);
            blit(graphics, "caidangui", 352, 186, 110, 148);
            blit(graphics, "lianqi_gui_slot", 438, 192, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.tray()) return;
        CompoundTag recipe = CookingRecipes.data(menu.recipe());
        graphics.drawString(font, recipe.getString("MenuName"), 362, 192, -26368, false);
        String container = recipe.getString("needcontainer");
        Component containerName = container.isEmpty() || container.equals("无")
                ? Component.translatable("gui.yvanchuxiuzhen.cooking.steamer.no_container")
                : CookingRecipes.stack(container.toLowerCase(Locale.ROOT)).getDisplayName();
        graphics.drawString(font, containerName, 357, 211, -16724788, false);
        for (int ingredient = 0; ingredient < 9; ingredient++) {
            String identity = recipe.getString("cailiao0" + ingredient);
            if (!identity.isEmpty()) graphics.drawString(font, CookingRecipes.stack(identity.toLowerCase(Locale.ROOT)).getDisplayName(),
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
