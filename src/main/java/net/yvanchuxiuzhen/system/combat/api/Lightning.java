package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.damage.LightningLanding;

/** 跨系统范围落雷入口，保留来源完整命令文字的参数解析、颜色与独立伤害开关。 */
public final class Lightning {
    private Lightning() {
    }

    public static void command(ServerLevel level, Vec3 center, String input) {
        LightningLanding.execute(level, center, input);
    }
}
