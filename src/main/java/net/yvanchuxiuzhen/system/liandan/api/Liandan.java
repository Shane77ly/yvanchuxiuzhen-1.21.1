package net.yvanchuxiuzhen.system.liandan.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.liandan.LiandanHerbAnalysis;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockEntity;

/** 炼丹系统的跨系统写入口，管理命令和火力符分别调用所属数据的操作。 */
public final class Liandan {
    private Liandan() {
    }

    public static void boostHeat(ServerLevel level, BlockPos pos, double power) {
        if (level.getBlockEntity(pos) instanceof AlchemyBlockEntity furnace) {
            furnace.getPersistentData().putDouble("firepower", power);
            furnace.changed();
        }
    }

    public static void unlockHerbs(ServerPlayer player) {
        LiandanHerbAnalysis.unlockAll(player);
    }
}
