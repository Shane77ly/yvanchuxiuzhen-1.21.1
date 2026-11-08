package net.yvanchuxiuzhen.system.equipment.client;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;

/** 来源过程动画请求发生时，将主副手当前帧与前一帧的持物高度均设为一。 */
final class EquipmentItemAnimationClient {
    private static final Field MAIN_HAND_HEIGHT = field("mainHandHeight");
    private static final Field OLD_MAIN_HAND_HEIGHT = field("oMainHandHeight");
    private static final Field OFF_HAND_HEIGHT = field("offHandHeight");
    private static final Field OLD_OFF_HAND_HEIGHT = field("oOffHandHeight");

    private EquipmentItemAnimationClient() {
    }

    static void resetHandHeight() {
        ItemInHandRenderer renderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;
        try {
            MAIN_HAND_HEIGHT.setFloat(renderer, 1);
            OLD_MAIN_HAND_HEIGHT.setFloat(renderer, 1);
            OFF_HAND_HEIGHT.setFloat(renderer, 1);
            OLD_OFF_HAND_HEIGHT.setFloat(renderer, 1);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("无法更新动画物品的持物高度", exception);
        }
    }

    private static Field field(String name) {
        try {
            Field field = ItemInHandRenderer.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
