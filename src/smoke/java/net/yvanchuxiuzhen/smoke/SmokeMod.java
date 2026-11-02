package net.yvanchuxiuzhen.smoke;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** 开发用客户端冒烟检查的入口；只在设置系统属性 yvanchuxiuzhen.smoke 时工作。 */
@Mod(value = "yvanchuxiuzhen_smoke", dist = Dist.CLIENT)
public final class SmokeMod {
    public SmokeMod() {
        String output = System.getProperty("yvanchuxiuzhen.smoke");
        if (output != null) {
            SmokeRun run = new SmokeRun(output);
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> run.tick());
        }
    }
}
