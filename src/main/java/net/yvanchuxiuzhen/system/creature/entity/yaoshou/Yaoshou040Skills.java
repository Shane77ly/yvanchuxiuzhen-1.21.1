package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 039与040按来源创建游离光和落阳实体。 */
final class Yaoshou040Skills {
    private Yaoshou040Skills() { }

    static Yaoshou040skill0301Entity projectile(ServerLevel level, Entity owner) {
        Yaoshou040skill0301Entity projectile = CreatureSetup.<Yaoshou040skill0301Entity>entityType("projectile_yaoshou_040skill_0301").get().create(level);
        projectile.setOwner(owner);
        projectile.setBaseDamage(0.1);
        projectile.setSilent(true);
        return projectile;
    }

    static void fallingSun(ServerLevel level, Entity owner, Vec3 position) {
        Yaoshou040skill02Entity sun = CreatureSetup.<Yaoshou040skill02Entity>entityType("yaoshou_040skill_02").get().create(level);
        sun.moveTo(position.x, position.y, position.z, level.getRandom().nextFloat() * 360F, 0);
        Relations.shareEntityAllegiance(owner, sun);
        sun.getPersistentData().putString("zhurenuid", owner.getStringUUID());
        sun.finalizeSpawn(level, level.getCurrentDifficultyAt(sun.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(sun);
    }
}
