package net.yvanchuxiuzhen.system.attribute.api;

import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.world.entity.Entity;
import net.yvanchuxiuzhen.system.attribute.data.AttributeAttachments;

/**
 * 非玩家实体的属性读写入口；统一由属性系统持有 {@code entity_stats} 附件，供修士、妖兽、战斗与法阵读取和更新。
 */
public final class EntityAttributes {
    /** 非玩家实体使用的十三项基础与战斗属性顺序。 */
    public static final List<Stat> COMBAT_STATS = List.of(
            Stat.HEALTH_MAX, Stat.HEALTH_REC, Stat.POWER_MAX, Stat.POWER_REC, Stat.SOUL_MAX, Stat.SOUL_REC,
            Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF, Stat.MOVE_SPEED
    );

    private EntityAttributes() {
    }

    public static boolean hasStats(Entity entity) {
        return entity.hasData(AttributeAttachments.ENTITY_STATS);
    }

    public static EntityStats get(Entity entity) {
        return entity.getData(AttributeAttachments.ENTITY_STATS);
    }

    public static void set(Entity entity, EntityStats stats) {
        entity.setData(AttributeAttachments.ENTITY_STATS, stats);
    }

    public static void update(Entity entity, UnaryOperator<EntityStats> updater) {
        set(entity, updater.apply(get(entity)));
    }

    public static double level(Entity entity) {
        return get(entity).level();
    }

    public static void setLevel(Entity entity, double level) {
        update(entity, stats -> stats.withLevel(level));
    }

    public static String realmName(Entity entity) {
        return get(entity).realmName();
    }

    public static void setRealmName(Entity entity, String realmName) {
        update(entity, stats -> stats.withRealmName(realmName));
    }

    public static double experience(Entity entity) {
        return get(entity).experience();
    }

    public static void setExperience(Entity entity, double experience) {
        update(entity, stats -> stats.withExperience(experience));
    }

    public static double experienceMax(Entity entity) {
        return get(entity).experienceMax();
    }

    public static void setExperienceMax(Entity entity, double experienceMax) {
        update(entity, stats -> stats.withExperienceMax(experienceMax));
    }

    public static double power(Entity entity) {
        return get(entity).power();
    }

    public static void setPower(Entity entity, double power) {
        update(entity, stats -> stats.withPower(power));
    }

    public static double stat(Entity entity, Stat stat) {
        return get(entity).stat(stat);
    }

    public static void setStat(Entity entity, Stat stat, double value) {
        update(entity, stats -> stats.withStat(stat, value));
    }

    public static String rating(Entity entity, Stat stat) {
        return get(entity).rating(stat);
    }

    public static void setRating(Entity entity, Stat stat, String rating) {
        update(entity, stats -> stats.withRating(stat, rating));
    }
}
