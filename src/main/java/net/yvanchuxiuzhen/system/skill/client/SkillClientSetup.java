package net.yvanchuxiuzhen.system.skill.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.network.SkillActionPayload;
import net.yvanchuxiuzhen.system.skill.projectile.ElementProjectiles;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
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
        modBus.addListener(SkillClientSetup::registerRenderers);
        NeoForge.EVENT_BUS.addListener(SkillClientSetup::onClientTick);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CANCEL);
        event.register(NEXT_PAGE);
    }

    /** 技能弹体按对应物品的图标渲染（来源弹体实现 {@code ItemSupplier}）。 */
    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (String id : ElementProjectiles.ids()) {
            event.registerEntityRenderer(SkillSetup.<SkillProjectile>entityType(id).get(), ThrownItemRenderer::new);
        }
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
