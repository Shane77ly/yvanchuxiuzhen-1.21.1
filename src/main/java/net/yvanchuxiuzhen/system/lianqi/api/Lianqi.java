package net.yvanchuxiuzhen.system.lianqi.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.yvanchuxiuzhen.system.lianqi.content.TripodBlockEntity;

/** 炼器系统对外提供的鼎炉火力写入口。 */
public final class Lianqi {
    private Lianqi() {
    }

    /** 火力符覆盖炉内火力，不重置已流逝的火力计时。 */
    public static void boostHeat(ServerLevel level, BlockPos pos, double power) {
        if (level.getBlockEntity(pos) instanceof TripodBlockEntity furnace) {
            furnace.getPersistentData().putDouble("firepower", power);
            furnace.changed();
        }
    }
}
