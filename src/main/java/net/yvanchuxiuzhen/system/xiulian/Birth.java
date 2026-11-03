package net.yvanchuxiuzhen.system.xiulian;

import java.util.List;

/**
 * 出身（来源 {@code birth} 字段的取值）。顺序是出身界面“下一个”的循环次序（来源 {@code BirthUp}），
 * 星数是界面上显示的星（来源 {@code Starxianshi01}–{@code 05}），说明分三行显示（来源 {@code Jointxt12}–{@code 14}）。
 */
public enum Birth {
    SHIJIA("世家纨绔", 0, "你是一名修仙大家族中，一名长老的嫡系，过着无忧", "无虑的纨绔生活，直到你想要到处游历（体验生活）。", " "),
    LUOPO("落魄家族", 1, "你是一名家道中落的修仙世家的嫡系后代，如今只有", "最后一点家当了，但仍然比别人更富有。", " "),
    FANREN("凡人奇遇", 2, "你是一名普普通通的凡人，在偶然机遇下，找到了一", "个修仙者的储物袋，从此开始修仙之旅。", " "),
    SHIHUN("失魂士兵", 4, "你是一名凡人国度的士兵，但却在最近一次大战中被", "当作炮灰，现在身负重伤。", " "),
    TAXIANG("他乡故知", 3, "好消息，你是一名大宗门长老的后代，坏消息，长老", "和别人下界私奔了，更坏的消息，你的出生是个意外。", "好在长老在陨落前留给你一个升仙令。"),
    WULIANG("无良商贾", 3, "你是一名村民骨折机发家的大商人，偶然找到一张藏", "宝图。", " "),
    YUANGU("远古秘辛", 2, "你在偶然之间获得了一张写着上古修士的坐化之地坐", "标的玉简，那里凶险至极，要等到自身足够强大后再", "去探索。"),
    LIULANG("流浪侠客", 5, "你是一名侠客，但是在拯救一个村庄后，你就上了灾", "厄村民的必杀榜。知恩图报的村民给你的一个远古石", "盘，这个石盘将是你的救命稻草。");

    private final String title;
    private final int stars;
    private final List<String> description;

    Birth(String title, int stars, String line1, String line2, String line3) {
        this.title = title;
        this.stars = stars;
        this.description = List.of(line1, line2, line3);
    }

    public String title() {
        return title;
    }

    public int stars() {
        return stars;
    }

    public List<String> description() {
        return description;
    }

    /** 按名字查找；不是这八种出身时为 null（来源界面此时不显示星与说明）。 */
    public static Birth of(String title) {
        for (Birth birth : values()) {
            if (birth.title.equals(title)) {
                return birth;
            }
        }
        return null;
    }

    public Birth next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public Birth previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }
}
