package net.yvanchuxiuzhen.system.creature.entity;

import net.yvanchuxiuzhen.platform.content.ContentTables;

/** 飞箭豪猪骨刺的独立实体工厂装配。 */
public final class Yaoshou046Projectiles {
    private Yaoshou046Projectiles() { }

    public static void register(ContentTables content) {
        content.entity("projectile_yaoshou_046skill_01", Yaoshou046skill01Entity::new);
    }
}
