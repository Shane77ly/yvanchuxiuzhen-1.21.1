package net.yvanchuxiuzhen.system.attribute;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.attribute.rule.AttributeTotals;

/**
 * 把最终值写入原版属性的基础值，物品与效果的修饰符在其上继续生效（来源同样使用 setBaseValue）。
 * 最大生命与攻击伤害按最终值写入，含防具与武器的炼器数值，与显示一致（Q17，负责人 2026-11-03 裁定）。
 * 最大生命降低时，当前生命由原版在属性更新时压到新上限。
 */
public final class VanillaAttributes {
    private VanillaAttributes() {
    }

    public static void apply(ServerPlayer player, AttributeTotals totals) {
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(totals.value(Stat.HEALTH_MAX));
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(totals.value(Stat.ATTACK));
        player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(totals.value(Stat.MOVE_SPEED));
    }
}
