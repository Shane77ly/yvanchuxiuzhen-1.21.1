package net.yvanchuxiuzhen.system.creature;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.yvanchuxiuzhen.system.plant.api.WildPlantCreatureEvent;

/** 魔眼破坏和血桃树苗计划刻的实体生成消费者。 */
final class WildPlantCreatures {
    private WildPlantCreatures() {
    }

    static void spawn(WildPlantCreatureEvent event) {
        ServerLevel level = event.level();
        BlockPos pos = event.pos();
        Mob creature;
        if (event.kind() == WildPlantCreatureEvent.Kind.DEMON_EYE) {
            creature = CreatureSetup.<Mob>entityType("moshou_003").get().create(level);
            creature.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, level.random.nextFloat() * 360, 0);
            creature.setTarget(event.target());
        } else {
            creature = CreatureSetup.<Mob>entityType("taomujing").get().create(level);
            creature.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new Random().nextInt(1, 361), 0);
            creature.setYBodyRot(new Random().nextInt(1, 361));
            creature.setYHeadRot(new Random().nextInt(1, 361));
        }
        creature.finalizeSpawn(level, level.getCurrentDifficultyAt(creature.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(creature);
    }
}
