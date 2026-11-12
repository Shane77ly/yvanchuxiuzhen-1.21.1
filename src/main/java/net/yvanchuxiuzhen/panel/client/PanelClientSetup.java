package net.yvanchuxiuzhen.panel.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.panel.network.OpenPanelPayload;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import org.lwjgl.glfw.GLFW;

/** 玩家 HUD 的装配与绘制。技能冷却、格挡和弓蓄力只读取本人同步数据，不在渲染回调推进玩法。 */
public final class PanelClientSetup {
    private static final KeyMapping MENU = new KeyMapping("key.yvanchuxiuzhen.guicaidan", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.misc") {
        @Override
        public void setDown(boolean value) {
            boolean pressed = value && !isDown();
            super.setDown(value);
            Minecraft minecraft = Minecraft.getInstance();
            if (pressed && minecraft.player != null && minecraft.screen == null) PacketDistributor.sendToServer(new OpenPanelPayload());
        }
    };
    private static final String[] COOLDOWN_NAMES = {"金", "水", "木", "火", "土", "阴阳"};
    private static final int[] COOLDOWN_COLORS = {-154, -10053121, -10027162, -39322, -39169, -1};

    private PanelClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(PanelClientSetup::registerLayers);
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(MENU));
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(PanelNavigation.ATTRIBUTES.get(), AttributeScreen::new));
    }

    private static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "skill_hud"), PanelClientSetup::renderSkill);
    }

    private static void renderSkill(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        double guard = Skills.guardRemaining(player);
        if (guard > 0) {
            blit(graphics, "gedang%02d".formatted(Math.min(10, (int) (guard / 2) + 1)), x - 52, y - 12, 103, 26);
        }
        double draw = Cultivation.tizhiDouble(player, "lagongtick");
        if (draw >= 1) {
            blit(graphics, "jinengxvlijieduan%02d".formatted(Math.min(10, (int) Math.ceil(draw / 10))), x - 1, y - 13, 18, 29);
        }
        for (int index = 0; index < SkillElement.values().length; index++) {
            double remaining = Skills.cooldown(player, SkillElement.values()[index]);
            if (remaining > 0) {
                graphics.drawString(minecraft.font, COOLDOWN_NAMES[index] + "冷却" + remaining / 20, x + 189, y + 86 + index * 9, COOLDOWN_COLORS[index], false);
            }
        }
        RenderSystem.disableBlend();
    }

    private static void blit(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        graphics.blit(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + name + ".png"), x, y, 0, 0, width, height, width, height);
    }
}
