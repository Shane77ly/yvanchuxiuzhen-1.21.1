package net.yvanchuxiuzhen.system.skill.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.system.skill.network.SkillActionPayload;
import org.lwjgl.glfw.GLFW;

/**
 * 技能系统的客户端装配：技能按键。B 取消选择（来源 {@code Jinengqvxiao}），Tab 翻页（来源 {@code Tabanjian}）；
 * 按下时只向服务端发送动作，状态由服务端判定。
 */
public final class SkillClientSetup {
    private static final String CATEGORY = "key.categories.misc";
    private static final KeyMapping CANCEL = new KeyMapping("key.yvanchuxiuzhen.jinengqvxiao", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);
    private static final KeyMapping NEXT_PAGE = new KeyMapping("key.yvanchuxiuzhen.tabanjian", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB, CATEGORY);

    private SkillClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(SkillClientSetup::registerKeys);
        NeoForge.EVENT_BUS.addListener(SkillClientSetup::onClientTick);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CANCEL);
        event.register(NEXT_PAGE);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        while (CANCEL.consumeClick()) {
            PacketDistributor.sendToServer(new SkillActionPayload(SkillActionPayload.Action.CANCEL));
        }
        while (NEXT_PAGE.consumeClick()) {
            PacketDistributor.sendToServer(new SkillActionPayload(SkillActionPayload.Action.NEXT_PAGE));
        }
    }
}
