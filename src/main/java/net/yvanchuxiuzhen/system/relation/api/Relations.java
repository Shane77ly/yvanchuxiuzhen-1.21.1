package net.yvanchuxiuzhen.system.relation.api;

import java.util.Set;
import java.util.HashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.relation.data.Allegiance;
import net.yvanchuxiuzhen.system.relation.data.RelationAttachments;

/**
 * 关系系统的公开操作：团队与敌友判定、给非玩家实体设置阵营。
 * 判定规则（来源 {@code Friendpanding*}）：判定方的友方集合含对方的团队名即为友方；玩家的集合含本人团队（登录时加入）。
 * 普通写操作只用于非玩家实体；驯服与玩家本体团队刷新入口只写实体字段，不改玩家能力的团队与好友。
 */
public final class Relations {
    private Relations() {
    }

    /** 实体的团队名；未设置时为空串。 */
    public static String team(Entity entity) {
        return allegiance(entity).team();
    }

    public static String temporaryTeam(Player player) {
        return player.getData(RelationAttachments.TEMPORARY_TEAM);
    }

    public static void setTemporaryTeam(ServerPlayer player, String team) {
        player.setData(RelationAttachments.TEMPORARY_TEAM, team);
        player.syncData(RelationAttachments.TEMPORARY_TEAM);
    }

    /** 来源Teamleave先以旧队名发送提示，再清空玩家能力临时队伍并同步给本人。 */
    public static void leaveTemporaryTeam(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.temporary_team.left", temporaryTeam(player)), false);
        setTemporaryTeam(player, "");
    }

    /** 来源本体实体 teamwork；副本维度进入与结束只操作这一份字段。 */
    public static String entityTemporaryTeam(Entity entity) {
        return entity.getData(RelationAttachments.ENTITY_TEMPORARY_TEAM);
    }

    public static void setEntityTemporaryTeam(Entity entity, String team) {
        entity.setData(RelationAttachments.ENTITY_TEMPORARY_TEAM, team);
    }

    /** {@code judge} 是否把 {@code other} 视为友方。 */
    public static boolean isFriendly(Entity judge, Entity other) {
        return allegiance(judge).isFriendly(team(other));
    }

    /** 友方集合的只读视图；来源丹火直接持有原表引用，重载前随主人关系变化。 */
    public static Set<String> friendlyTeams(Entity owner) {
        return allegiance(owner).friendlyTeams();
    }

    public static boolean isFriendly(Set<String> friendlyTeams, Entity other) {
        return friendlyTeams.contains(team(other));
    }

    /** 来源直接读取实体 NBT team 的路径；玩家的能力团队与本体实体字段分别保存。 */
    public static String entityTeam(Entity entity) {
        return entity.getData(RelationAttachments.ENTITY).team();
    }

    /** 来源登录、重生及存活玩家每秒恢复时，把能力团队写回本体实体团队。 */
    public static void refreshEntityTeam(ServerPlayer player) {
        player.getData(RelationAttachments.ENTITY).setTeam(team(player));
    }

    /** 召唤物、投射物等继承 {@code owner} 的团队与友方集合（来源复制施放者的 team 与 friendlist）。 */
    public static void copyAllegiance(Entity owner, Entity target) {
        entityAllegiance(target).copyFrom(allegiance(owner));
    }

    /** 非玩家目标直接共享施放者的来源friendlist引用；团队值单独复制，存档重载后集合独立。 */
    public static void shareEntityAllegiance(Entity owner, Entity target) {
        entityAllegiance(target).shareSourceFrom(allegiance(owner));
    }

    /** 出窍本体与玩家共享友表，并以其中的 playeruuid 关联受伤、死亡和归窍。 */
    public static void initializeProjection(Entity owner, Entity target) {
        Allegiance friends = entityAllegiance(target);
        friends.shareSourceFrom(allegiance(owner));
        friends.setFriendString("playeruuid", owner.getStringUUID());
    }

    public static String projectionOwner(Entity target) {
        return entityAllegiance(target).friendString("playeruuid");
    }

    /** 冰系实体使用友方子表保存的整数伤害参数；首个实体与来源子表共享引用。 */
    public static void initializeIceCast(Entity owner, Entity target, int attackDamage, int multiplier) {
        Allegiance friends = entityAllegiance(target);
        friends.shareSourceFrom(allegiance(owner));
        friends.setFriendInt("AttackDamage", attackDamage);
        friends.setFriendString("AttackUUID", owner.getStringUUID());
        friends.setFriendInt("Damage_multiplier", multiplier);
    }

    public record IceCast(int attackDamage, String attacker, int multiplier, int direction) {
    }

    /** 读取实体友表。 */
    public static IceCast iceCast(Entity entity) {
        Allegiance friends = entityAllegiance(entity);
        return new IceCast(friends.friendInt("AttackDamage"), friends.friendString("AttackUUID"),
                friends.friendInt("Damage_multiplier"), friends.friendInt("jiaodu"));
    }

    public static void setIceCastDirection(Entity target, int direction) {
        entityAllegiance(target).setFriendInt("jiaodu", direction);
    }

    /** 后续冰刺的阵营已由调用者复制，只修改副本中的剩余数。 */
    public static void setIceCastRemaining(Entity target, int remaining) {
        entityAllegiance(target).setFriendInt("AttackDamage", remaining);
    }

    /** 水系施放把参数写入取得的友表，并将同一子表交给技能实体。 */
    public static void initializeWaterCast(Entity owner, Entity target, int damage, int multiplier, String mode) {
        Allegiance friends = entityAllegiance(target);
        friends.shareSourceFrom(allegiance(owner));
        friends.setFriendInt("AttackDamage", damage);
        friends.setFriendString("AttackUUID", owner.getStringUUID());
        friends.setFriendString("damagetype", "水");
        friends.setFriendInt("Damage_multiplier", multiplier);
        if (mode != null) {
            friends.setFriendString("jinengName", mode);
        }
    }

    /** 木04和水04施放先复制来源友表，再写入攻击参数和当前模式。 */
    public static void initializeCopiedSkillCast(Entity owner, Entity target, int damage, int multiplier, String mode) {
        Allegiance friends = entityAllegiance(target);
        friends.copyFrom(allegiance(owner));
        friends.setFriendInt("AttackDamage", damage);
        friends.setFriendString("AttackUUID", owner.getStringUUID());
        friends.setFriendInt("Damage_multiplier", multiplier);
        friends.setFriendString("jinengName", mode);
    }

    /** 同次乱枝施放的后续实体取得首实体使用的同一份复制表。 */
    public static void shareTreeCast(Entity source, Entity target, int damage, String attacker, int multiplier, String mode) {
        Allegiance friends = entityAllegiance(target);
        friends.shareFrom(entityAllegiance(source));
        friends.setFriendInt("AttackDamage", damage);
        friends.setFriendString("AttackUUID", attacker);
        friends.setFriendInt("Damage_multiplier", multiplier);
        friends.setFriendString("jinengName", mode);
    }

    /** 乱枝潜行对友方枝条复制友表并写入标记。 */
    public static void markTreeTangle(Entity target) {
        friendCast(target);
        Allegiance friends = entityAllegiance(target);
        friends.copyFrom(friends);
        friends.setFriendBoolean("baolie", true);
    }

    public record FriendCast(int damage, String attacker, int multiplier, String damageType, String mode) {
    }

    /** 读取技能实体当前友表中的来源字段。 */
    public static FriendCast friendCast(Entity target) {
        Allegiance friends = entityAllegiance(target);
        return new FriendCast(friends.friendInt("AttackDamage"), friends.friendString("AttackUUID"),
                friends.friendInt("Damage_multiplier"), friends.friendString("damagetype"), friends.friendString("jinengName"));
    }

    /** 后续木04实体复制父级友表后只改写技能名称。 */
    public static void setFriendCastMode(Entity target, String mode) {
        entityAllegiance(target).setFriendString("jinengName", mode);
    }

    /** 水04引爆前复制原友表，避免改变其他实体持有的表。 */
    public static void detonateWaterOrb(Entity target) {
        Allegiance friends = entityAllegiance(target);
        friends.copyFrom(friends);
        friends.setFriendBoolean("baolie", true);
    }

    public static boolean waterOrbDetonated(Entity target) {
        return entityAllegiance(target).friendBoolean("baolie");
    }

    public static void setWaterOrbScale(Entity target, double scale) {
        entityAllegiance(target).setFriendDouble("pengzhuangxiang", scale);
    }

    public static void restoreWaterOrbScale(Entity target, double scale) {
        Allegiance friends = entityAllegiance(target);
        if (!friends.hasFriendKey("pengzhuangxiang")) {
            friends.setFriendDouble("pengzhuangxiang", scale);
        }
    }

    public static double waterOrbScale(Entity target) {
        double scale = entityAllegiance(target).friendDouble("pengzhuangxiang");
        return scale <= 0.1 ? 1 : scale;
    }

    /** 迁入旧目标存档的施放快照；新格式或已有友表字段不被覆盖。 */
    public static void restoreFriendCast(Entity target, int damage, String attacker, int multiplier, String damageType, String mode) {
        Allegiance friends = entityAllegiance(target);
        if (!friends.hasFriendKey("AttackDamage") && !friends.hasFriendKey("AttackUUID")
                && !friends.hasFriendKey("Damage_multiplier") && !friends.hasFriendKey("damagetype")
                && !friends.hasFriendKey("jinengName")) {
            friends.setFriendInt("AttackDamage", damage);
            friends.setFriendString("AttackUUID", attacker);
            friends.setFriendInt("Damage_multiplier", multiplier);
            friends.setFriendString("damagetype", damageType);
            friends.setFriendString("jinengName", mode);
        }
    }

    /** 地上蛊罐把友方标签引用交给虫，虫的团队值来自该虫物品实例。 */
    public static void shareBlockAllegiance(CompoundTag blockData, Entity target, String team) {
        entityAllegiance(target).shareFromBlock(blockData, team);
    }

    /** 放置蛊罐时共享主人的友方标签；方块单独持久化，重载后与主人分离。 */
    public static void shareAllegianceToBlock(Entity owner, CompoundTag blockData) {
        allegiance(owner).shareToBlock(blockData);
    }

    /** 灵纹生成时把施放者的好友子表交给特效实体，保留运行期引用共享。 */
    public static void initializeRune(Entity owner, Entity target, int enchantmentLevel) {
        Allegiance friends = entityAllegiance(target);
        friends.shareSourceFrom(allegiance(owner));
        friends.setFriendInt("friendnumber", enchantmentLevel);
        friends.setFriendInt("lingwennum", friends.friendInt("lingwennum") + 1);
        friends.setFriendString("belongToMasterUUID", owner.getStringUUID());
    }

    /** 来源命中遍历读取实体本体子表，包括玩家本体而非玩家能力表。 */
    public static void incrementRuneForOwner(Entity target, Entity owner) {
        Allegiance friends = target.getData(RelationAttachments.ENTITY);
        if (friends.friendString("belongToMasterUUID").equals(owner.getStringUUID())
                && friends.friendInt("lingwennum") < 6) {
            friends.setFriendInt("lingwennum", friends.friendInt("lingwennum") + 1);
        }
    }

    /** 将旧目标版本灵纹实体的独立计数迁入友表，不覆盖已有新格式字段。 */
    public static void restoreLegacyRune(Entity entity, int count, String owner) {
        Allegiance friends = entityAllegiance(entity);
        if (!friends.hasFriendKey("lingwennum")) {
            friends.setFriendInt("lingwennum", count);
        }
        if (!friends.hasFriendKey("belongToMasterUUID") && !owner.isEmpty()) {
            friends.setFriendString("belongToMasterUUID", owner);
        }
    }

    public static int runeCount(Entity entity) {
        return entity.getData(RelationAttachments.ENTITY).friendInt("lingwennum");
    }

    public static void setRuneCount(Entity entity, int count) {
        entity.getData(RelationAttachments.ENTITY).setFriendInt("lingwennum", count);
    }

    public static String runeOwner(Entity entity) {
        return entity.getData(RelationAttachments.ENTITY).friendString("belongToMasterUUID");
    }

    /** 设置非玩家实体的团队名，友方集合不变。 */
    public static void setTeam(Entity target, String team) {
        entityAllegiance(target).setTeam(team);
    }

    /** 神傀符驯服：复制目标的玩家能力好友表（非玩家为空），移除旧实体团队并断开原集合引用。 */
    public static void replaceTamingFriends(Entity target) {
        Set<String> friends = target instanceof Player ? new HashSet<>(friendlyTeams(target)) : new HashSet<>();
        friends.remove(entityTeam(target));
        target.getData(RelationAttachments.ENTITY).replace(entityTeam(target), friends);
    }

    /** 驯服后只写本体实体团队；来源取施放者的玩家能力团队，非玩家施放者取空串。 */
    public static void setTamingTeam(Entity owner, Entity target) {
        target.getData(RelationAttachments.ENTITY).setTeam(owner instanceof Player ? team(owner) : "");
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
