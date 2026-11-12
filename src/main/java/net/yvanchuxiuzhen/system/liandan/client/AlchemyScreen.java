package net.yvanchuxiuzhen.system.liandan.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyMenu;
import net.yvanchuxiuzhen.system.liandan.network.AlchemyButtonPayload;

/** 炼丹界面沿用来源画布、贴图及按钮位置，读取菜单库存和丹炉同步数据展示进度与丹方。 */
public final class AlchemyScreen extends AbstractContainerScreen<AlchemyMenu> {
    public AlchemyScreen(AlchemyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        addButton(68, 126, 31, 18, "daochu01", 0, "按丹方装料");
        addButton(103, 126, 31, 18, "daoru01", 1, "保存丹方");
        addButton(227, 199, 44, 26, "lianzhikaishi01", 2, "开始炼丹");
        addButton(43, 237, 18, 18, "lianqishuominganniu09", 3, "炼丹说明");
        addButton(43, 257, 18, 18, "lianqishuominganniu07", 4, "药材说明");
        addButton(438, 216, 12, 12, "bankgui10", 5, "丹炉属性说明");
    }

    private void addButton(int x, int y, int width, int height, String name, int id, String message) {
        Button button = new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_" + name), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new AlchemyButtonPayload(id)));
        button.setMessage(Component.literal(message));
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
        blit(graphics, "liandan_gui", 43, 121, 414, 214);
        blit(graphics, "lianqi_gui_slot", 169, 228, 18, 18);
        for (int slot = 1; slot <= 8; slot++) {
            if (!menu.getSlot(slot - 1).getItem().isEmpty()) {
                blit(graphics, "lianqi_gui_slot", 169 + slot * 18, 228, 18, 18);
            }
        }
        blit(graphics, "liandan_gui_02", 170, 229, 16, 16);
        int progress = menu.progress();
        for (int segment = 0; segment < 10; segment++) {
            if (progress >= (segment == 0 ? 1 : segment * 10)) {
                blit(graphics, "lianqi_gui_jindu", 160 + segment * 18, 125, 18, 18);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        CompoundTag data = menu.data();
        graphics.drawString(font, menu.furnaceName(), 366, 129, -13312, false);
        graphics.drawString(font, Component.translatable("lianqibiao01").getString() + "：" + data.getInt("lianpower"), 366, 147, -16711732, false);
        graphics.drawString(font, "丹炉等级：" + data.getInt("alchemyLevel"), 366, 160, -26368, false);
        graphics.drawString(font, "丹炉火力：" + data.getInt("alchemyFire"), 366, 171, -26368, false);
        graphics.drawString(font, "分丹能力：" + data.getInt("alchemyDannum"), 366, 182, -26368, false);
        ItemStack bottle = menu.getSlot(0).getItem();
        String container = bottle.is(AlchemyMenu.BOTTLES) ? "§f容器：" + bottle.getDisplayName().getString() : "§4请在第一个位置放置一个容器！";
        graphics.drawString(font, container, 51, 147, -10066330, false);
        ItemStack recipe = menu.getSlot(10).getItem();
        CompoundTag recipeData = recipe.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        for (int slot = 1; slot <= 8; slot++) {
            String name = recipeData.getString("cailiao0" + slot + "name");
            String line = name.isEmpty() || name.equals("无") ? "§4无"
                    : (recipe.isEmpty() || menu.itemId(slot).equals(name) ? "§f" : "§4") + name;
            graphics.drawString(font, line, 51, 147 + slot * 9, -10066330, false);
        }
    }

    private void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(texture(name), leftPos + x, topPos + y, 0, 0, width, height, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }
}
