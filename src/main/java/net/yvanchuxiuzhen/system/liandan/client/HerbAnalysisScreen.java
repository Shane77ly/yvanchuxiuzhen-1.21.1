package net.yvanchuxiuzhen.system.liandan.client;

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
import net.yvanchuxiuzhen.system.liandan.LiandanHerbAnalysis;
import net.yvanchuxiuzhen.system.liandan.content.HerbAnalysisMenu;
import net.yvanchuxiuzhen.system.liandan.data.HerbAnalysis;
import net.yvanchuxiuzhen.system.liandan.network.HerbAnalysisPayload;

/** 百草鉴按来源贴图和坐标绘制玩家解析缓存，两个按钮只向服务端发送操作编号。 */
public final class HerbAnalysisScreen extends AbstractContainerScreen<HerbAnalysisMenu> {
    private static final ResourceLocation BACKGROUND = texture("baicaojian");
    private static final ResourceLocation SHEET = texture("baicaogui01");

    public HerbAnalysisScreen(HerbAnalysisMenu menu, Inventory inventory, Component title) {
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
        addButton(47, -13, "imagebutton_shuaxin01", 0);
        addButton(-7, 22, "imagebutton_baicaoguianniu01", 1);
    }

    private void addButton(int x, int y, String name, int button) {
        Button control = new TextureButton(leftPos + x, topPos + y, 16, 16, texture("atlas/" + name), 16, 32,
                pressed -> PacketDistributor.sendToServer(new HerbAnalysisPayload(menu.containerId, button)));
        control.setMessage(Component.literal(button == 0 ? "刷新药草" : "辨识药草"));
        addRenderableWidget(control);
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
        HerbAnalysis analysis = LiandanHerbAnalysis.analysis(minecraft.player);
        graphics.drawString(font, analysis.selectedName(), -3, -27, -52, false);
        graphics.drawString(font, analysis.progressText(), -3, 7, -16777012, false);
        graphics.drawString(font, analysis.needText(), 5, 26, -13421773, false);
        for (int ordinal = 1; ordinal <= 10; ordinal++) {
            graphics.drawString(font, analysis.effectText(ordinal), 93, -27 + (ordinal - 1) * 10, -12829636, false);
        }
        graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.baicaojian.label_yao_xiao_mei_100dian_zhuan_huan_wei_1ji_xiao_guo_deng_ji"),
                0, 167, -6710887, false);
        graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.baicaojian.label_shi_jian_mei_20ke_huan_suan_wei_1miao_zhong"),
                0, 177, -6710887, false);
        graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.baicaojian.label_shun_shi_yao_xiao_hui_mei_you_chi_xu_shi_jian_zhe_ge_shu_xing_zhe_shi_zheng_chang_de"),
                0, 187, -6710887, false);
    }
}
