package net.yvanchuxiuzhen.system.gongfa.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 功法加成公式的算例，按来源 {@code GongfaHealthUp} 等过程手算：基础值按层数分段，乘档位系数后向上取整（移速不取整）；
 * 研习时的初值（{@code Gongfashuxingjisuan}：生命 64、攻击 4.8、移速 0.04 × 系数）等于层数 1 的值。
 */
class TechniqueStatTest {
    @Test
    void studyValuesAreLevelOne() {
        assertEquals(39, TechniqueStat.HEALTH_MAX.value(1, "最下档"));
        assertEquals(4, TechniqueStat.ATTACK.value(1, "下档"));
        assertEquals(2, TechniqueStat.HEALTH_REC.value(1, "上档"));
        assertEquals(0.04 * 1.4, TechniqueStat.MOVE_SPEED.value(1, "最上档"), 1e-12);
    }

    /** 生命：层数 15 中档 108 + 5 × 12 = 168；层数 31 最上档 (452 + 20) × 1.4 = 660.8，取整 661。 */
    @Test
    void segmentedGrowth() {
        assertEquals(168, TechniqueStat.HEALTH_MAX.value(15, "中档"));
        assertEquals(661, TechniqueStat.HEALTH_MAX.value(31, "最上档"));
        assertEquals(17, TechniqueStat.ATTACK.value(21, "下档"));
        assertEquals(36, TechniqueStat.POWER_REC.value(12, "中档"));
    }

    @Test
    void unknownTierGivesZero() {
        assertEquals(0, TechniqueStat.SOUL_MAX.value(10, ""));
    }
}
