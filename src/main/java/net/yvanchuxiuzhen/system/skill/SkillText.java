package net.yvanchuxiuzhen.system.skill;

/**
 * 技能书的品阶文字与所需境界文字（来源 {@code JinengGO} 等生成命令按数值写入 {@code belongTopinjietext}、
 * {@code belongToLeveltext}）：品阶 0 为无品阶，1–10 黄阶、11–20 玄阶、21–30 地阶、31–40 天阶，31 以上不是 31–40 的整数时为超品；
 * 境界 0 为凡人，1–9 练气、10 练气圆满，11–20 筑基、21–30 结丹、31–40 元婴，31 以上不是 31–40 的整数时为化神。其余非整数的数值来源不写文字。
 */
public final class SkillText {
    private static final String[] NUMERALS = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};

    private SkillText() {
    }

    public static String grade(double grade) {
        if (grade >= 31) {
            return grade <= 40 && grade == Math.floor(grade) ? "天阶" + NUMERALS[(int) grade - 31] + "级" : "超品";
        }
        if (grade >= 21) {
            return grade == Math.floor(grade) ? "地阶" + NUMERALS[(int) grade - 21] + "级" : "";
        }
        if (grade >= 11) {
            return grade == Math.floor(grade) ? "玄阶" + NUMERALS[(int) grade - 11] + "级" : "";
        }
        if (grade == 0) {
            return "无品阶";
        }
        return grade >= 1 && grade <= 10 && grade == Math.floor(grade) ? "黄阶" + NUMERALS[(int) grade - 1] + "级" : "";
    }

    public static String level(double level) {
        if (level >= 31) {
            return level <= 40 && level == Math.floor(level) ? "元婴" + NUMERALS[(int) level - 31] + "重" : "化神";
        }
        if (level >= 21) {
            return level == Math.floor(level) ? "结丹" + NUMERALS[(int) level - 21] + "转" : "";
        }
        if (level >= 11) {
            return level == Math.floor(level) ? "筑基" + NUMERALS[(int) level - 11] + "阶" : "";
        }
        if (level == 0) {
            return "凡人";
        }
        if (level == 10) {
            return "练气圆满";
        }
        return level >= 1 && level <= 9 && level == Math.floor(level) ? "练气" + NUMERALS[(int) level - 1] + "层" : "";
    }
}
