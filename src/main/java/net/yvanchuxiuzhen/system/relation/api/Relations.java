package net.yvanchuxiuzhen.system.relation.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.relation.data.Allegiance;
import net.yvanchuxiuzhen.system.relation.data.RelationAttachments;

/**
 * 关系系统的公开操作：团队与敌友判定、给非玩家实体设置阵营。
 * 判定规则（来源 {@code Friendpanding*}）：判定方的友方集合含对方的团队名即为友方；玩家的集合含本人团队（登录时加入）。
 * 写操作只用于非玩家实体，玩家的团队与好友由本系统的界面流程维护。
 */
public final class Relations {
    private Relations() {
    }

    /** 实体的团队名；未设置时为空串。 */
    public static String team(Entity entity) {
        return allegiance(entity).team();
    }

    /** {@code judge} 是否把 {@code other} 视为友方。 */
    public static boolean isFriendly(Entity judge, Entity other) {
        return allegiance(judge).isFriendly(team(other));
    }

    /** 召唤物、投射物等继承 {@code owner} 的团队与友方集合（来源复制施放者的 team 与 friendlist）。 */
    public static void copyAllegiance(Entity owner, Entity target) {
        entityAllegiance(target).copyFrom(allegiance(owner));
    }

    /** 设置非玩家实体的团队名，友方集合不变。 */
    public static void setTeam(Entity target, String team) {
        entityAllegiance(target).setTeam(team);
    }

    /** 把 {@code team} 加入非玩家实体的友方集合。 */
    public static void addFriendlyTeam(Entity target, String team) {
        entityAllegiance(target).setFriendly(team, true);
    }

    private static Allegiance allegiance(Entity entity) {
        return entity instanceof Player player ? player.getData(RelationAttachments.PLAYER).allegiance() : entity.getData(RelationAttachments.ENTITY);
    }

    private static Allegiance entityAllegiance(Entity target) {
        if (target instanceof Player) {
            throw new IllegalArgumentException("玩家的阵营由关系系统维护：" + target);
        }
        return target.getData(RelationAttachments.ENTITY);
    }
}
