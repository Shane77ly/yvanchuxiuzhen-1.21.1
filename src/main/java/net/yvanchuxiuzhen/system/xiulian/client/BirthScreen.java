package net.yvanchuxiuzhen.system.xiulian.client;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.xiulian.Birth;
import net.yvanchuxiuzhen.system.xiulian.BirthSelection;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.content.BirthMenu;
import net.yvanchuxiuzhen.system.xiulian.network.BirthButtonPayload;

/**
 * 出身界面（来源 {@code PlayerJoinGUIScreen}，文字过程 {@code Jointxt1}–{@code 14}、星 {@code Starxianshi01}–{@code 05}），
 * 坐标、颜色与贴图沿用来源：名字、寿命、五行灵根与锁定按钮、剩余锁定与随机次数、体质、出身切换与说明。
 */
public final class BirthScreen extends AbstractContainerScreen<BirthMenu> {
    private static final ResourceLocation BACKGROUND = texture("playerbegin");
    private static final ResourceLocation STAR = texture("star");
    private static final List<String> ROOT_LABELS = List.of("§6金:", "§1水:", "§2木:", "§4火:", "§5土:");
    private static final int GRAY = -12829636;

    public BirthScreen(BirthMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png");
    }

    @Override
    protected void init() {
        super.init();
        String[] locks = {"imagebutton_lock1", "imagebutton_lock11", "imagebutton_lock12", "imagebutton_lock13", "imagebutton_lock14"};
        for (int i = 0; i < locks.length; i++) {
            addButton(178, 179 + i * 18, 16, 16, locks[i], i);
        }
        addButton(168, 156, 16, 16, "imagebutton_shuaxin01", 5);
        addButton(213, 273, 8, 10, "imagebutton_gui0401down01", 6);
        addButton(276, 273, 8, 10, "imagebutton_gui0401up01", 7);
    }

    private void addButton(int x, int y, int width, int height, String name, int button) {
        addRenderableWidget(new TextureButton(leftPos + x, topPos + y, width, height, texture("atlas/" + name), width, height * 2,
                pressed -> PacketDistributor.sendToServer(new BirthButtonPayload(button))));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos + 96, topPos + 138, 0, 0, 260, 220, 260, 220);
        Birth birth = Birth.of(Cultivation.get(minecraft.player, CultivationText.BIRTH));
        int stars = birth == null ? 0 : birth.stars();
        for (int i = 0; i < stars; i++) {
            guiGraphics.blit(STAR, leftPos + 283 + i * 14, topPos + 269, 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Player player = minecraft.player;
        double lifetime = Cultivation.get(player, CultivationValue.LIFETIME);
        guiGraphics.drawString(font, player.getDisplayName().getString(), 186, 156, -6724096, false);
        guiGraphics.drawString(font, "寿命约" + whole(Math.floor(lifetime / 365)) + "年(" + whole(lifetime) + "日)", 186, 165, GRAY, false);
        guiGraphics.drawString(font, "剩余锁定次数:" + whole(Cultivation.get(player, CultivationValue.LIFTLOCK)), 276, 156, GRAY, false);
        guiGraphics.drawString(font, "§0剩余随机次数：" + whole(Cultivation.get(player, CultivationValue.RANDOMLIFT)), 276, 165, GRAY, false);
        for (int i = 0; i < ROOT_LABELS.size(); i++) {
            String text = ROOT_LABELS.get(i) + whole(Cultivation.get(player, BirthSelection.ROOTS.get(i)));
            if (Cultivation.tizhiBoolean(player, BirthSelection.LOCK_KEYS.get(i))) {
                text += "§0(已锁定)";
            }
            guiGraphics.drawString(font, text, 195, 183 + i * 18, GRAY, false);
        }
        String constitution = Cultivation.get(player, CultivationText.CONSTITUTION);
        guiGraphics.drawString(font, constitution.isEmpty() ? "体质：无" : "体质：" + constitution, 276, 183, GRAY, false);
        String birthTitle = Cultivation.get(player, CultivationText.BIRTH);
        guiGraphics.drawString(font, birthTitle, 231, 273, GRAY, false);
        Birth birth = Birth.of(birthTitle);
        if (birth != null) {
            for (int line = 0; line < birth.description().size(); line++) {
                guiGraphics.drawString(font, birth.description().get(line), 150, 288 + line * 9, GRAY, false);
            }
        }
    }

    /** 来源用 {@code DecimalFormat("0")} 显示，按四舍六入五成双取整。 */
    private static String whole(double value) {
        return new DecimalFormat("0").format(value);
    }
}
