package net.yvanchuxiuzhen.system.liandan.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockEntity;

/** 炼丹装置的跨系统写入口，火力符只替换增益强度，保留已累计的有效时长。 */
public final class Liandan {
    private Liandan() {
    }

    public static void boostHeat(ServerLevel level, BlockPos pos, double power) {
        if (level.getBlockEntity(pos) instanceof AlchemyBlockEntity furnace) {
            furnace.getPersistentData().putDouble("firepower", power);
            furnace.changed();
        }
    }
}
