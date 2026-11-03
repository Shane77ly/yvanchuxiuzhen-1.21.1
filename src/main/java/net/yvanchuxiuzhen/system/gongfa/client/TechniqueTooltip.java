package net.yvanchuxiuzhen.system.gongfa.client;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;

/**
 * 功法书的提示文本（来源 {@code ItemLore} 的功法分支与 {@code GongfaLore}）：本人研习过的书显示品阶与“重天”、经验与所需经验，
 * 其余只显示品阶；随后是简介，以及有数值的六组加成。每组的颜色比较书与功法栏中书的档位：栏中更高为红、更低为绿，相同或栏空为白。
 */
public final class TechniqueTooltip {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");
    private static final List<Pair> PAIRS = List.of(
            new Pair(TechniqueStat.HEALTH_MAX, "血量上限增加：", TechniqueStat.HEALTH_REC, "（血量恢复增加："),
            new Pair(TechniqueStat.POWER_MAX, "灵力上限增加：", TechniqueStat.POWER_REC, "（灵力恢复增加："),
            new Pair(TechniqueStat.SOUL_MAX, "精力上限增加：", TechniqueStat.SOUL_REC, "（精力恢复增加："),
            new Pair(TechniqueStat.ATTACK, "物理攻击增加：", TechniqueStat.ATTACK_DEF, "（物理防御增加："),
            new Pair(TechniqueStat.MANA_ATT, "法术攻击增加：", TechniqueStat.MANA_DEF, "（法术防御增加："),
            new Pair(TechniqueStat.SOUL_ATT, "精神攻击增加：", TechniqueStat.SOUL_DEF, "（精神防御增加："));

    private record Pair(TechniqueStat first, String firstLabel, TechniqueStat second, String secondLabel) {
    }

    private TechniqueTooltip() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }
        Techniques.book(event.getItemStack()).ifPresent(book -> append(event.getToolTip(), player, book));
    }

    private static void append(List<Component> tooltip, Player player, TechniqueBook book) {
        if (book.ownedBy(player.getUUID()) && book.level() >= 1) {
            tooltip.add(Component.literal("§6" + book.definition().gradeText() + "(" + WHOLE.format(book.level()) + "重天)"));
            tooltip.add(Component.literal("§b(" + WHOLE.format(book.exp()) + "/" + WHOLE.format(book.expMax()) + ")"));
        } else {
            tooltip.add(Component.literal("§6" + book.definition().gradeText()));
        }
        tooltip.add(Component.literal("§f" + book.definition().intro()));
        boolean slotEmpty = Techniques.slot(player).isEmpty();
        TechniqueBook equipped = Techniques.main(player).orElse(null);
        for (Pair pair : PAIRS) {
            if (book.value(pair.first()) > 0) {
                tooltip.add(Component.literal(color(slotEmpty, equipped, book, pair.first()) + pair.firstLabel() + book.value(pair.first())
                        + color(slotEmpty, equipped, book, pair.second()) + pair.secondLabel() + book.value(pair.second()) + "）"));
            }
        }
    }

    private static String color(boolean slotEmpty, TechniqueBook equipped, TechniqueBook book, TechniqueStat stat) {
        if (slotEmpty) {
            return "§f";
        }
        int mine = rank(equipped == null ? "" : equipped.definition().tier(stat));
        int theirs = rank(book.definition().tier(stat));
        return mine > theirs ? "§4" : mine < theirs ? "§a" : "§f";
    }

    /** 档位等级：最上档 5、上档 4、中档 3、下档 2，其他 1。 */
    private static int rank(String tier) {
        return switch (tier) {
            case "最上档" -> 5;
            case "上档" -> 4;
            case "中档" -> 3;
            case "下档" -> 2;
            default -> 1;
        };
    }
}
