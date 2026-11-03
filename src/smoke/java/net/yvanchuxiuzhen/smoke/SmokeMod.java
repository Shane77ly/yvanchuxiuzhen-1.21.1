package net.yvanchuxiuzhen.smoke;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 开发用客户端冒烟检查的入口；只在设置系统属性 yvanchuxiuzhen.smoke 时工作。
 * 检查自动运行，不需要鼠标：每 tick 开始时把窗口记为非活动，{@code MouseHandler.grabMouse} 因此不捕获鼠标，
 * 开发者在检查期间可以继续使用鼠标；失去焦点时不暂停游戏。
 */
@Mod(value = "yvanchuxiuzhen_smoke", dist = Dist.CLIENT)
public final class SmokeMod {
    public SmokeMod() {
        String output = System.getProperty("yvanchuxiuzhen.smoke");
        if (output != null) {
            SmokeRun run = new SmokeRun(output);
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> releaseMouse());
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> run.tick());
        }
    }

    private static void releaseMouse() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.options.pauseOnLostFocus = false;
        minecraft.setWindowActive(false);
        if (minecraft.mouseHandler.isMouseGrabbed()) {
            minecraft.mouseHandler.releaseMouse();
        }
    }
}
