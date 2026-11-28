package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.damage.LightningLanding;

/** 跨系统范围落雷入口，区分实体施法者、存档主人和无来源天劫。 */
public final class Lightning {
    private Lightning() {
    }

    public static void castSaved(ServerLevel level, Vec3 center, String owner, double base, double multiplier, String team, int diameter, String color, boolean fixed) {
        LightningLanding.castSaved(level, center, owner, base, multiplier, team, diameter, color, fixed);
    }

    public static void tribulation(ServerLevel level, Vec3 center, double base, double multiplier, int diameter, String color) {
        LightningLanding.tribulation(level, center, base, multiplier, diameter, color);
    }

    public static void cast(ServerLevel level, Vec3 center, Entity caster, double base, double multiplier, String team, int diameter, String color, boolean fixed) {
        LightningLanding.cast(level, center, caster, base, multiplier, team, diameter, color, fixed);
    }
}
