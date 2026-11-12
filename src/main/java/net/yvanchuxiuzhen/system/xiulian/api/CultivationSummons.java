package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.XiulianSetup;
import net.yvanchuxiuzhen.system.xiulian.tribulation.ThunderPillar;

/** 外部玩法按来源请求修炼系统的雷柱实体。 */
public final class CultivationSummons {
    private CultivationSummons() { }

    /** 妖兽040雷柱：目标四角各偏移五格，继承施放者团队并共享其运行期友方集合。 */
    public static void summonLightPillars(ServerLevel level, Entity owner, Entity target, double attack) {
        for (int[] offset : new int[][]{{5, 5}, {5, -5}, {-5, -5}, {-5, 5}}) {
            ThunderPillar pillar = XiulianSetup.<ThunderPillar>entityType("tianjie_05").get().create(level);
            pillar.moveTo(target.getX() + offset[0], target.getY(), target.getZ() + offset[1], level.getRandom().nextFloat() * 360F, 0);
            Relations.shareEntityAllegiance(owner, pillar);
            pillar.getPersistentData().putString("playerUUID", target.getStringUUID());
            pillar.getPersistentData().putDouble("thunder_Att", attack);
            pillar.finalizeSpawn(level, level.getCurrentDifficultyAt(pillar.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(pillar);
        }
    }
}
