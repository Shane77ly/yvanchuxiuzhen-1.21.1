package net.yvanchuxiuzhen.system.book.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.book.BookQuizQuestion;
import net.yvanchuxiuzhen.system.book.BookQuizQuestions;
import net.yvanchuxiuzhen.system.book.content.BookQuizMenu;
import net.yvanchuxiuzhen.system.book.data.BookQuizData;
import net.yvanchuxiuzhen.system.book.network.BookQuizButtonPayload;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 答题界面保留来源两层背景、四个选项图标与题数位置，无物品槽。 */
public final class BookQuizScreen extends AbstractContainerScreen<BookQuizMenu> {
    private static final ResourceLocation BACKGROUND = texture("dati_gui");
    private static final ResourceLocation SHEET = texture("bookgui");
    private static final List<String> BUTTON_TEXTURES = List.of("imagebutton_bookguianniu1", "imagebutton_bookguianniu11",
            "imagebutton_bookguianniu12", "imagebutton_bookguianniu13");
    private static final int[] OPTION_Y = {43, 70, 97, 124};
    private static final DecimalFormat WHOLE = new DecimalFormat("0");
    private final List<Button> options = new ArrayList<>();

    public BookQuizScreen(BookQuizMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
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
        options.clear();
        for (int index = 0; index < 4; index++) {
            int buttonId = index;
            Button button = new TextureButton(leftPos - 11, topPos + 34 + index * 27, 16, 16,
                    buttonTexture(BUTTON_TEXTURES.get(index)), 16, 32,
                    ignored -> answer(buttonId));
            options.add(button);
            addRenderableWidget(button);
        }
        updateOptionLabels();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateOptionLabels();
    }

    private void updateOptionLabels() {
        BookQuizQuestion question = BookQuizQuestions.get(BookQuizData.state(minecraft.player).questionId());
        for (int index = 0; index < options.size(); index++) {
            options.get(index).setMessage(Component.literal(question.options().get(index)));
        }
    }

    private void answer(int buttonId) {
        PacketDistributor.sendToServer(new BookQuizButtonPayload(menu.containerId, buttonId));
        if (BookQuizQuestions.get(BookQuizData.state(minecraft.player).questionId()).answer() == buttonId + 1) {
            // 来源按钮在本地执行的环形粒子只对答题本人可见，计数、生命与书籍结果仍由服务端提交。
            double angle = 0;
            for (int index = 0; index < 72; index++) {
                angle += 5;
                double x = Math.cos(angle);
                double z = Math.sin(angle);
                minecraft.level.addParticle(Particles.type("tiangandizhi"), minecraft.player.getX() + x,
                        minecraft.player.getY() + 1, minecraft.player.getZ() + z, x * 0.1, 0, z * 0.1);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        graphics.blit(SHEET, leftPos - 42, topPos - 23, 0, 0, 260, 213, 260, 213);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int questionId = BookQuizData.state(minecraft.player).questionId();
        BookQuizQuestion question = BookQuizQuestions.get(questionId);
        graphics.drawString(font, question.prompt(), -2, -2, -16777063, false);
        for (int index = 0; index < question.options().size(); index++) {
            graphics.drawString(font, question.options().get(index), 15, OPTION_Y[index], -13421773, false);
        }
        graphics.drawString(font, Component.translatable("screen.yvanchuxiuzhen.book_quiz_progress", WHOLE.format(BookQuizData.state(minecraft.player).progress())),
                69, 169, -16764007, false);
    }
}
