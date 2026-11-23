package net.yvanchuxiuzhen.platform.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import org.lwjgl.glfw.GLFW;

/** 两个独立键位在按下或松开时将当前组合发送给服务端。 */
public final class ModifierKeysClient {
    private static final HeldKey CTRL = new HeldKey("ctrlanjian", GLFW.GLFW_KEY_LEFT_CONTROL);
    private static final HeldKey ALT = new HeldKey("altanjian", GLFW.GLFW_KEY_F19);

    private ModifierKeysClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterKeyMappingsEvent event) -> {
            event.register(CTRL);
            event.register(ALT);
        });
    }

    private static final class HeldKey extends KeyMapping {
        private boolean held;

        private HeldKey(String name, int defaultKey) {
            super("key.yvanchuxiuzhen." + name, defaultKey, KeyMapping.CATEGORY_MISC);
        }

        @Override
        public void setDown(boolean down) {
            super.setDown(down);
            if (held == down) {
                return;
            }
            held = down;
            if (Minecraft.getInstance().player != null) {
                PacketDistributor.sendToServer(new ModifierKeys.StatePayload(CTRL.held, ALT.held));
            }
        }
    }
}
