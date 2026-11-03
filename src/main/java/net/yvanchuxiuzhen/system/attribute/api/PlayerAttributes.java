package net.yvanchuxiuzhen.system.attribute.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.attribute.VanillaAttributes;
import net.yvanchuxiuzhen.system.attribute.data.AttributeAttachments;
import net.yvanchuxiuzhen.system.attribute.data.AttributeData;
import net.yvanchuxiuzhen.system.attribute.rule.AttributeTotals;

/**
 * 属性系统的公开操作。贡献只在服务端提交，提交后立即重算、写入原版属性并同步给本人；
 * 客户端只能查询本人的值。来源键由提交者自定，同一键再次提交时覆盖上次的值。
 * 装备类贡献随附件跨越死亡保留，提交者在登录、重生与装备变化时按当前状态重新提交。
 */
public final class PlayerAttributes {
    private PlayerAttributes() {
    }

    /** 最终值（来源 {@code _display}）。 */
    public static double value(Player player, Stat stat) {
        return player.getData(AttributeAttachments.ATTRIBUTES).totals().value(stat);
    }

    /** 基础值：初始值加基础层贡献（来源中不带 {@code _display} 的字段）。 */
    public static double baseValue(Player player, Stat stat) {
        return player.getData(AttributeAttachments.ATTRIBUTES).totals().base(stat);
    }

    public static void setContribution(ServerPlayer player, String source, Contribution contribution) {
        update(player, player.getData(AttributeAttachments.ATTRIBUTES).with(source, contribution));
    }

    public static void clearContribution(ServerPlayer player, String source) {
        update(player, player.getData(AttributeAttachments.ATTRIBUTES).without(source));
    }

    /** 攻击方的五行系数项；非玩家攻击方没有灵根，为 0（来源读到 PlayerVariables 的默认值）。 */
    public static double elementCoefficient(Entity attacker, DamageElement element) {
        return attacker instanceof Player player ? AttributeTotals.elementCoefficient(value(player, element.root())) : 0;
    }

    /** 受击方的功法五行项；阴、阳与非玩家受击方为 0。 */
    public static double receivedElementBonus(Entity target, DamageElement element) {
        if (element.gongfa() == null || !(target instanceof Player player)) {
            return 0;
        }
        return AttributeTotals.receivedElementBonus(value(player, element.gongfa()), value(player, Stat.GONGFA_LEVEL));
    }

    private static void update(ServerPlayer player, AttributeData data) {
        player.setData(AttributeAttachments.ATTRIBUTES, data);
        VanillaAttributes.apply(player, data.totals());
    }
}
