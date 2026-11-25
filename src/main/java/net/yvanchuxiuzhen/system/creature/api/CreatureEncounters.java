package net.yvanchuxiuzhen.system.creature.api;

import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.EventHooks;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;

/** 璞玉奇遇妖兽的生成操作。 */
public final class CreatureEncounters {
    private CreatureEncounters() {
    }

    public static void spawnJadeEncounter(ServerPlayer player, int fortune) {
        String id = fortune == 103 ? "yaoshou_045" : "yaoshou_041";
        ServerLevel level = player.serverLevel();
        Mob creature = CreatureSetup.<Mob>entityType(id).get().create(level);
        creature.moveTo(player.getX(), player.getY(), player.getZ(), level.getRandom().nextFloat() * 360F, 0F);
        EntityAttributes.setLevel(creature, new Random().nextInt(1, 36));
        EventHooks.finalizeMobSpawn(creature, level, level.getCurrentDifficultyAt(creature.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(creature);
    }
}
