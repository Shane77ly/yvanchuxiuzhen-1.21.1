package net.yvanchuxiuzhen.system.attribute.rule;

import java.util.List;
import java.util.Map;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 属性汇总与五行公式的算例，数值取自 docs/systems/attribute/architecture.md“来源问题”中 Q17、Q18 的算例。
 */
class AttributeTotalsTest {
    /** Q17：基础生命 20，胸甲炼器 +10、饰品 +5，最终值 35；基础值不含加成层。 */
    @Test
    void bonusLayerCountsInValueButNotBase() {
        AttributeTotals totals = new AttributeTotals(List.of(
                new Contribution(Layer.BONUS, Map.of(Stat.HEALTH_MAX, 10.0)),
                new Contribution(Layer.BONUS, Map.of(Stat.HEALTH_MAX, 5.0))));
        assertEquals(20, totals.base(Stat.HEALTH_MAX));
        assertEquals(35, totals.value(Stat.HEALTH_MAX));
    }

    /** 初始攻击 1，修炼成长 +4 为基础值 5；武器炼器 +8 后最终值 13，战斗中只读基础值的位置仍得到 5。 */
    @Test
    void baseLayerCountsInBoth() {
        AttributeTotals totals = new AttributeTotals(List.of(
                new Contribution(Layer.BASE, Map.of(Stat.ATTACK, 4.0)),
                new Contribution(Layer.BONUS, Map.of(Stat.ATTACK, 8.0))));
        assertEquals(5, totals.base(Stat.ATTACK));
        assertEquals(13, totals.value(Stat.ATTACK));
    }

    /** Q18：火属性伤害 10、攻击方火灵根 3 时为 10 ×（0.3 + 1）= 13；受击方主修火功法 5、层数 8 时再加 10，共 23。 */
    @Test
    void fireDamageExample() {
        double damage = 10 * (AttributeTotals.elementCoefficient(3) + 1) + AttributeTotals.receivedElementBonus(5, 8);
        assertEquals(23, damage, 1e-9);
    }
}
