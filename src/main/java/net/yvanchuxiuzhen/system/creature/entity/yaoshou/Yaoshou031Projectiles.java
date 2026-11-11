package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;

/** 031的独立来源水流弹体工厂；目标注册ID沿用048skill01来源身份。 */
public final class Yaoshou031Projectiles {
    private Yaoshou031Projectiles() { }

    public static void register(ContentTables content) {
        content.entity("projectile_yaoshou_048skill_01", Yaoshou031WaterProjectile::new);
    }

    static Yaoshou031WaterProjectile create(ServerLevel level, Entity owner) {
        Yaoshou031WaterProjectile projectile = CreatureSetup.<Yaoshou031WaterProjectile>entityType("projectile_yaoshou_048skill_01")
                .get().create(level);
        projectile.setOwner(owner);
        projectile.setBaseDamage(0.1);
        projectile.setSilent(true);
        return projectile;
    }
}
