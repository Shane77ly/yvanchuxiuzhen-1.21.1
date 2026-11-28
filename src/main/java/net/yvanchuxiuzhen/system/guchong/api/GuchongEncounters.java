package net.yvanchuxiuzhen.system.guchong.api;

import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.EventHooks;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.guchong.GuchongSetup;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong04Entity;

/** 璞玉奇遇的蛊虫生成操作。 */
public final class GuchongEncounters {
    private GuchongEncounters() {
    }

    /** 普通雕琢命中奇遇105后，在玩家位置生成一只蛊虫04。出生初始化按预设等级计算属性。 */
    public static void spawnJadeEncounter(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Guchong04Entity insect = GuchongSetup.<Guchong04Entity>entityType("guchong_04").get().create(level);
        insect.moveTo(player.getX(), player.getY(), player.getZ(), level.getRandom().nextFloat() * 360F, 0F);
        EntityAttributes.setLevel(insect, new Random().nextInt(1, 36));
        EventHooks.finalizeMobSpawn(insect, level, level.getCurrentDifficultyAt(insect.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(insect);
    }
}
