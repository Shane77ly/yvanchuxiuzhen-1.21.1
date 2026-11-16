package net.yvanchuxiuzhen.collection.decor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 九种木材的匾额载体与牌位共用文字数据，保留独立方块和方块实体身份。 */
public record InscribedDecorVariant(String id, int wood, boolean memorial) {
    public static final List<InscribedDecorVariant> VALUES = variants();

    private static List<InscribedDecorVariant> variants() {
        List<InscribedDecorVariant> result = new ArrayList<>();
        for (int wood = 1; wood <= 9; wood++) {
            String number = String.format(Locale.ROOT, "%02d", wood);
            result.add(new InscribedDecorVariant("plaque_" + number + "item", wood, false));
            result.add(new InscribedDecorVariant("paiwei_" + number, wood, true));
        }
        return List.copyOf(result);
    }

    public String factory() { return "decor:inscribed_" + id; }
    public String itemId() { return memorial ? id + "item" : id; }
    public boolean comparator() { return !memorial && wood <= 2; }
    public String segment(int part) { return String.format(Locale.ROOT, "plaque_%02d%02d", wood, part); }
}
