package net.yvanchuxiuzhen.system.world.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.world.StructureExploration;

/** 遗迹搜索的公开操作；位置取调用脚本的执行锚点，进度由世界系统维护。 */
public final class StructureSearch {
    private StructureSearch() { }

    public static void search(ServerLevel level, Vec3 position, String playerUuid) {
        StructureExploration.search(level, position, playerUuid);
    }
}
