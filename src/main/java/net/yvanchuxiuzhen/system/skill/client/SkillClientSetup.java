package net.yvanchuxiuzhen.system.skill.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillLook;
import net.yvanchuxiuzhen.system.skill.network.SkillActionPayload;
import net.yvanchuxiuzhen.system.skill.projectile.ElementProjectiles;
import net.yvanchuxiuzhen.system.skill.projectile.SkillArrows;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.GeoAnimatable;

/**
 * 技能系统的客户端装配。B 取消、Tab 翻页、W/S/A/D/空格身法、F 轻功；每次从松开变为按下时只向服务端发送动作。
 */
public final class SkillClientSetup {
    private static final String CATEGORY = "key.categories.misc";
    private static final KeyMapping CANCEL = new ActionKey("jinengqvxiao", GLFW.GLFW_KEY_B, SkillActionPayload.Action.CANCEL);
    private static final KeyMapping NEXT_PAGE = new ActionKey("tabanjian", GLFW.GLFW_KEY_TAB, SkillActionPayload.Action.NEXT_PAGE);
    private static final KeyMapping FORWARD = new ActionKey("anjian_w", GLFW.GLFW_KEY_W, SkillActionPayload.Action.MOVE_W);
    private static final KeyMapping BACK = new ActionKey("anjian_s", GLFW.GLFW_KEY_S, SkillActionPayload.Action.MOVE_S);
    private static final KeyMapping LEFT = new ActionKey("anjian_a", GLFW.GLFW_KEY_A, SkillActionPayload.Action.MOVE_A);
    private static final KeyMapping RIGHT = new ActionKey("anjian_d", GLFW.GLFW_KEY_D, SkillActionPayload.Action.MOVE_D);
    private static final KeyMapping JUMP = new ActionKey("anjian_space", GLFW.GLFW_KEY_SPACE, SkillActionPayload.Action.JUMP);
    private static final KeyMapping FLIGHT = new ActionKey("fanjian", GLFW.GLFW_KEY_F, SkillActionPayload.Action.TOGGLE_FLIGHT);

    private SkillClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(SkillClientSetup::registerKeys);
        modBus.addListener(SkillClientSetup::registerRenderers);
        modBus.addListener(SkillClientSetup::registerLayers);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CANCEL);
        event.register(NEXT_PAGE);
        event.register(FORWARD);
        event.register(BACK);
        event.register(LEFT);
        event.register(RIGHT);
        event.register(JUMP);
        event.register(FLIGHT);
    }

    /** 元素弹体按对应物品图标渲染，技能箭用原生模型，技能动画实体用 GeckoLib。 */
    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (String id : ElementProjectiles.ids()) {
            event.registerEntityRenderer(SkillSetup.<SkillProjectile>entityType(id).get(), ThrownItemRenderer::new);
        }
        for (int kind = 1; kind <= 10; kind++) {
            int number = kind;
            event.registerEntityRenderer(SkillSetup.<SkillProjectile>entityType(SkillArrows.id(kind)).get(), context -> new SkillArrowRenderer(context, number));
        }
        for (EntityModelRow row : SkillEntities.models().values()) {
            registerAnimated(event, row);
        }
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (int kind = 1; kind <= 10; kind++) {
            int number = kind;
            event.registerLayerDefinition(SkillArrowRenderer.layer(kind), () -> SkillArrowModels.layer(number));
        }
    }

    private static <T extends Entity & GeoAnimatable & SkillLook> void registerAnimated(EntityRenderersEvent.RegisterRenderers event, EntityModelRow row) {
        Supplier<EntityType<T>> type = SkillSetup.entityType(row.id());
        if (type != null) {
            event.registerEntityRenderer(type.get(), context -> new SkillEntityRenderer<T>(context, row));
        }
    }

    /** 保留来源 setDown 的按下沿语义，长按产生的键盘重复事件不重复施放，也不消费原版移动键。 */
    private static final class ActionKey extends KeyMapping {
        private final SkillActionPayload.Action action;

        private ActionKey(String name, int key, SkillActionPayload.Action action) {
            super("key.yvanchuxiuzhen." + name, InputConstants.Type.KEYSYM, key, CATEGORY);
            this.action = action;
        }

        @Override
        public void setDown(boolean value) {
            boolean pressed = value && !isDown();
            super.setDown(value);
            Minecraft minecraft = Minecraft.getInstance();
            if (pressed && minecraft.player != null && minecraft.screen == null) {
                PacketDistributor.sendToServer(new SkillActionPayload(action));
            }
        }
    }
}
