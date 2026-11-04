package net.yvanchuxiuzhen.platform.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import org.lwjgl.glfw.GLFW;

/** 客户端每刻查看左 Ctrl 与左 Alt 是否按住，变化时通知服务端（见 {@link ModifierKeys}）。 */
public final class ModifierKeysClient {
    private static boolean ctrl;
    private static boolean alt;

    private ModifierKeysClient() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ModifierKeysClient::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        long window = minecraft.getWindow().getWindow();
        boolean nowCtrl = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL);
        boolean nowAlt = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT);
        if (nowCtrl != ctrl || nowAlt != alt) {
            ctrl = nowCtrl;
            alt = nowAlt;
            PacketDistributor.sendToServer(new ModifierKeys.StatePayload(ctrl, alt));
        }
    }
}
