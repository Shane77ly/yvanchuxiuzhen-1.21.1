package net.yvanchuxiuzhen.system.xiulian;

import java.util.List;

/**
 * 体质（来源 {@code constitution} 字段的取值）与出身界面关闭时写入的修正（来源 {@code PlayerJoin02} 后半段）。
 * 随机体质取 1–10（{@link #rolled}），来源列出的第 11、12 种取不到（Q21）；不是前 11 种的体质（含空体质）按第 12 种生效。
 */
public enum Constitution {
    TAICHU("太初混沌体", List.of(new Bonus("LevelupCorrectXiuweixishou", 0.02))),
    YINYANG("阴阳双生体", List.of()),
    DANFENG("丹凤流金体", List.of(new Bonus("GoldAttCorrectPercentage", 0.05))),
    XIANTIAN("先天纯水体", List.of(new Bonus("WaterAttCorrectPercentage", 0.05))),
    YIMU("乙木元阴体", List.of(new Bonus("TreeAttCorrectPercentage", 0.05))),
    XIULUO("修罗圣火体", List.of(new Bonus("FireAttCorrectPercentage", 0.05))),
    DAHUANG("大荒印道体", List.of(new Bonus("SoilAttCorrectPercentage", 0.05))),
    XIAGUANG("霞光幻灵体", List.of(new Bonus("LightAttCorrectPercentage", 0.05), new Bonus("LightDefCorrectPercentage", 0.05))),
    JIUTIAN("九天映霆体", List.of(new Bonus("ThunderAttCorrectPercentage", 0.05), new Bonus("ThunderDefCorrectPercentage", 0.05))),
    YUANJIN("渊尽寒寂体", List.of(new Bonus("IceAttCorrectPercentage", 0.05), new Bonus("IceDefCorrectPercentage", 0.05))),
    FENGDONG("风动玉髓体", List.of(new Bonus("WindAttCorrectPercentage", 0.05), new Bonus("WindDefCorrectPercentage", 0.05))),
    CHUNYANG("纯阳合剑体", List.of(new Bonus("AttackAttCorrectPercentage", 0.05)));

    /** 阴阳双生体另把修为效率 {@code experience_rate} 加 0.3。 */
    public static final double YINYANG_EXPERIENCE_RATE = 0.3;

    private final String title;
    private final List<Bonus> bonuses;

    Constitution(String title, List<Bonus> bonuses) {
        this.title = title;
        this.bonuses = bonuses;
    }

    /** 修正值键与加值。 */
    public record Bonus(String key, double amount) {
    }

    public String title() {
        return title;
    }

    public List<Bonus> bonuses() {
        return bonuses;
    }

    /** 打开的体质标记 {@code tizhiopen_001}–{@code 012}。 */
    public String openKey() {
        return String.format("tizhiopen_%03d", ordinal() + 1);
    }

    /** 随机数 1–10 对应的体质。 */
    public static Constitution rolled(int roll) {
        return values()[roll - 1];
    }

    /** 关闭出身界面时按名字生效的体质；不是前 11 种时为纯阳合剑体。 */
    public static Constitution applied(String title) {
        for (Constitution constitution : values()) {
            if (constitution.title.equals(title)) {
                return constitution;
            }
        }
        return CHUNYANG;
    }
}
