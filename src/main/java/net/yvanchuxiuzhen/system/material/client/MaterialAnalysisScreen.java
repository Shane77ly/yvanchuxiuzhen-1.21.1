package net.yvanchuxiuzhen.system.material.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.material.MaterialLibrary;
import net.yvanchuxiuzhen.system.material.api.MaterialRevealedEntry;
import net.yvanchuxiuzhen.system.material.content.MaterialAnalysisMenu;
import net.yvanchuxiuzhen.system.material.data.MaterialAnalysis;
import net.yvanchuxiuzhen.system.material.network.MaterialAnalysisButtonPayload;

/** 千材录沿用来源原生贴图、槽位、四按钮与十行效果坐标，只显示服务端已刷新和揭示的数据。 */
public final class MaterialAnalysisScreen extends AbstractContainerScreen<MaterialAnalysisMenu> {
    private static final ResourceLocation BACKGROUND = texture("qiancailu_gui");
    private static final ResourceLocation SHEET = texture("baicaogui01");

    public MaterialAnalysisScreen(MaterialAnalysisMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }

    @Override
    protected void init() {
        super.init();
        addButton(53, -13, 16, "imagebutton_bankgui14", 0);
        addButton(-2, 25, 16, "imagebutton_baicaoguianniu03", 1);
        addButton(195, -38, 12, "imagebutton_bankgui10", 2);
        addButton(195, -20, 12, "imagebutton_bankgui101", 3);
    }

    private void addButton(int x, int y, int size, String name, int button) {
        addRenderableWidget(new TextureButton(leftPos + x, topPos + y, size, size, texture("atlas/" + name), size, size * 2,
                pressed -> PacketDistributor.sendToServer(new MaterialAnalysisButtonPayload(menu.containerId, button))));
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
        graphics.blit(SHEET, leftPos - 12, topPos - 39, 0, 0, 202, 120, 202, 120);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.selectedName().isEmpty()) return;
        MaterialAnalysis analysis = MaterialLibrary.analysis(minecraft.player);
        int progress = analysis.progress(menu.selectedId());
        int maximum = analysis.maximum(menu.selectedId());
        graphics.drawString(font, "解析进度：" + progress + "/" + maximum, -2, 7, -13421569, false);
        graphics.drawString(font, menu.selectedName(), -2, -29, -154, false);
        if (progress >= 1 && progress >= maximum) {
            graphics.drawString(font, "此材料已完全解析。", 12, 29, -16777216, false);
        } else {
            graphics.drawString(font, "进一步解析此材料", 12, 29, -16777216, false);
            graphics.drawString(font, "需要消耗" + progress * 2 + "个", 12, 41, -16777216, false);
            graphics.drawString(font, menu.selectedName(), 12, 53, -16777216, false);
        }
        List<MaterialRevealedEntry> entries = analysis.revealed(menu.selectedId());
        for (int index = 0; index < Math.min(10, entries.size()); index++) {
            MaterialRevealedEntry entry = entries.get(index);
            String key;
            if (entry.namespace().equals("teshu")) key = "teshu.yvanchuxiuzhen." + entry.name();
            else if (!entry.open()) key = "attribute.yvanchuxiuzhen." + entry.name();
            else key = "enchantment." + entry.namespace() + "." + entry.name();
            graphics.drawString(font, Component.translatable(key).withStyle(ChatFormatting.AQUA), 91, -29 + index * 10, -12829636, false);
        }
    }
}
