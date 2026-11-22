package net.yvanchuxiuzhen.system.guchong.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.GuchongReleaseMenu;
import net.yvanchuxiuzhen.system.guchong.network.GuchongReleaseButtonPayload;

/** 蛊笛界面保持来源两层背景、六个蛊罐按钮与页码位置。 */
public final class GuchongReleaseScreen extends AbstractContainerScreen<GuchongReleaseMenu> {
    private static final ResourceLocation BACKGROUND = texture("guchongshifang_gui");
    private static final ResourceLocation SHEET = texture("yanggugui01");
    private static final List<String> BUTTON_TEXTURES = List.of("imagebutton_yanggugui06", "imagebutton_yanggugui061",
            "imagebutton_yanggugui062", "imagebutton_yanggugui063", "imagebutton_yanggugui064", "imagebutton_yanggugui065");
    private static final DecimalFormat WHOLE = new DecimalFormat("0");
    private final List<Button> jarButtons = new ArrayList<>();
    private final Player player;

    public GuchongReleaseScreen(GuchongReleaseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        player = inventory.player;
        imageWidth = 176;
        imageHeight = 168;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }

    private static ResourceLocation buttonTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/atlas/" + name + ".png");
    }

    @Override
    protected void init() {
        super.init();
        jarButtons.clear();
        Button down = new TextureButton(leftPos + 6, topPos + 62, 36, 15, buttonTexture("imagebutton_yanggugui02"),
                36, 30, ignored -> sendButton(0));
        down.setMessage(Component.translatable("screen.yvanchuxiuzhen.guchong_page_down"));
        addRenderableWidget(down);
        Button up = new TextureButton(leftPos + 132, topPos + 62, 36, 15, buttonTexture("imagebutton_yanggugui04"),
                36, 30, ignored -> sendButton(1));
        up.setMessage(Component.translatable("screen.yvanchuxiuzhen.guchong_page_up"));
        addRenderableWidget(up);
        for (int index = 0; index < GuchongReleaseMenu.DISPLAY_SLOTS; index++) {
            int button = index + 2;
            Button widget = new TextureButton(leftPos + 22 + index / 3 * 72, topPos - 25 + index % 3 * 27,
                    62, 22, buttonTexture(BUTTON_TEXTURES.get(index)), 62, 44, ignored -> sendButton(button));
            jarButtons.add(widget);
            addRenderableWidget(widget);
        }
        updateButtonLabels();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtonLabels();
    }

    private void updateButtonLabels() {
        CompoundTag state = GuchongJarData.read(player.getMainHandItem());
        for (int index = 0; index < jarButtons.size(); index++) {
            jarButtons.get(index).setMessage(Component.translatable("screen.yvanchuxiuzhen.guchong_jar_control",
                    index + 1, state.getString("xianshiweizhi" + index)));
        }
    }

    private void sendButton(int button) {
        PacketDistributor.sendToServer(new GuchongReleaseButtonPayload(menu.containerId, button));
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
        graphics.blit(SHEET, leftPos, topPos - 40, 0, 0, 176, 124, 176, 124);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        CompoundTag state = GuchongJarData.read(player.getMainHandItem());
        for (int index = 0; index < GuchongReleaseMenu.DISPLAY_SLOTS; index++) {
            graphics.drawString(font, state.getString("xianshiweizhi" + index), 46 + index / 3 * 72,
                    -17 + index % 3 * 27, -12829636, false);
        }
        graphics.drawString(font, WHOLE.format(state.getDouble("pagenum")), 85, 72, -1, false);
    }
}
