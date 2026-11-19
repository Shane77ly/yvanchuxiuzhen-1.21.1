package net.yvanchuxiuzhen.smoke;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 开发用客户端冒烟检查的入口；只在设置系统属性 yvanchuxiuzhen.smoke 时工作。
 * 检查在隐藏窗口中自动运行，输入由游戏内夹具产生。失去焦点时仍继续游戏刻。
 */
@Mod(value = "yvanchuxiuzhen_smoke", dist = Dist.CLIENT)
public final class SmokeMod {
    public SmokeMod() {
        String output = System.getProperty("yvanchuxiuzhen.smoke");
        if (output != null) {
            SmokeRun run = new SmokeRun(output);
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> Minecraft.getInstance().options.pauseOnLostFocus = false);
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> run.tick());
        }
    }
}
