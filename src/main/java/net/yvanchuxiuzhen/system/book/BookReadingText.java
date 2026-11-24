package net.yvanchuxiuzhen.system.book;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.book.api.BookTextData;
import net.yvanchuxiuzhen.system.book.api.BookTexts;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipe;
import net.yvanchuxiuzhen.system.lianqi.api.Lianqi;
import net.yvanchuxiuzhen.system.lianqi.api.LianqiBlueprint;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 来源 Bookwenzi1–11 的只读投影：每行独立遵循原分派次序，不修改业务实例。 */
public final class BookReadingText {
    private static final List<StatPair> PAIRS = List.of(
            new StatPair(TechniqueStat.HEALTH_MAX, "血量上限增加：", TechniqueStat.HEALTH_REC, "（血量恢复增加："),
            new StatPair(TechniqueStat.POWER_MAX, "灵力上限增加：", TechniqueStat.POWER_REC, "（灵力恢复增加："),
            new StatPair(TechniqueStat.SOUL_MAX, "精力上限增加：", TechniqueStat.SOUL_REC, "（精力恢复增加："),
            new StatPair(TechniqueStat.ATTACK, "物理攻击增加：", TechniqueStat.ATTACK_DEF, "（物理防御增加："),
            new StatPair(TechniqueStat.MANA_ATT, "法术攻击增加：", TechniqueStat.MANA_DEF, "（法术防御增加："),
            new StatPair(TechniqueStat.SOUL_ATT, "精神攻击增加：", TechniqueStat.SOUL_DEF, "（精神防御增加："));
    private static final String[] ELEMENT_NAMES = {"金", "水", "木", "火", "土", "阴阳"};

    private record StatPair(TechniqueStat first, String firstLabel, TechniqueStat second, String secondLabel) {
    }

    private BookReadingText() {
    }

    public static List<String> lines(Player player) {
        ItemStack stack = player.getMainHandItem();
        BookTextData text = BookTexts.get(stack);
        List<String> lines = new ArrayList<>(11);
        for (int index = 1; index <= 11; index++) lines.add(text.line(index));
        TechniqueBook technique = Techniques.book(stack).filter(book -> !book.definition().name().isEmpty()).orElse(null);
        SkillBook skill = Skills.book(stack).filter(book -> !book.name().isEmpty()).orElse(null);
        LianqiBlueprint blueprint = Lianqi.blueprint(stack).orElse(null);
        MedicineRecipe recipe = Liandan.recipe(stack);
        boolean isBlueprint = blueprint != null;
        boolean isRecipe = recipe.isPresent();
        if (technique != null) {
            lines.set(0, "§6" + technique.definition().name() + "(" + technique.definition().gradeText() + ")");
            lines.set(1, owner(technique.owner().isEmpty(), technique.ownerName()));
        } else if (skill != null) {
            lines.set(0, "§6" + skill.name() + "(" + skill.gradeText() + ")");
            String owner = owner(skill.owner().isEmpty(), skill.ownerName());
            String weapon = skill.kind() == SkillKind.SPELL ? skill.neededWeapon()
                    : skill.kind() == SkillKind.WEAPON_ART ? skill.text("smanshiptype") : "";
            lines.set(1, owner + (weapon.isEmpty() ? "" : " 需求武器：" + weapon));
        } else if (isBlueprint) {
            lines.set(0, "§6" + blueprint.name() + "(" + blueprint.levelText() + " " + blueprint.type() + ")");
            lines.set(1, "§3" + blueprint.displayDescription());
        } else if (isRecipe) {
            lines.set(0, "§6" + recipe.name());
            lines.set(1, "§3" + recipe.description());
        }
        DecimalFormat whole = new DecimalFormat("0");
        if (isBlueprint) {
            for (int index = 0; index < 9; index++) {
                LianqiBlueprint.Ingredient ingredient = blueprint.ingredient(index);
                lines.set(index + 2, ingredient.name() + "(" + whole.format(ingredient.count()) + ")");
            }
        } else if (isRecipe) {
            for (int index = 1; index <= 8; index++) lines.set(index + 1, recipe.ingredient(index).name());
        } else if (technique != null) {
            techniqueLines(lines, player, technique, whole);
        } else if (skill != null) {
            skillLines(lines, player, skill, whole);
        }
        return List.copyOf(lines);
    }

    private static String owner(boolean unowned, String name) {
        return "§3此书主人：" + (unowned ? "无" : name);
    }

    private static String body(String requirement) {
        if (requirement.isEmpty() || requirement.equals("无")) return "任何灵根都可修习。";
        return requirement.equals("五") ? "需要五行俱全才可研习。" : "需要身具" + requirement + "其中之一的灵根才可研习。";
    }

    private static String level(String requirement) {
        return requirement.equals("无") ? "凡人亦可修习。" : "需要达到" + requirement + "境界才可研习。";
    }

    private static void techniqueLines(List<String> lines, Player player, TechniqueBook book, DecimalFormat whole) {
        boolean owned = book.ownedBy(player.getUUID());
        lines.set(2, owned ? "修炼功法等级：" + whole.format(book.level()) + "（" + whole.format(book.exp()) + "/"
                + whole.format(book.expMax()) + "）" : body(book.definition().body()));
        for (int index = 3; index <= 9; index++) lines.set(index, "");
        if (!owned) {
            lines.set(3, level(book.definition().levelText()));
            return;
        }
        TechniqueBook equipped = Techniques.main(player).orElse(null);
        for (int index = 0; index < PAIRS.size(); index++) {
            StatPair pair = PAIRS.get(index);
            if (book.value(pair.first()) + book.value(pair.second()) > 0) {
                lines.set(index + 3, color(equipped, book, pair.first()) + pair.firstLabel() + book.value(pair.first())
                        + color(equipped, book, pair.second()) + pair.secondLabel() + book.value(pair.second()) + "）");
            }
        }
        if (book.value(TechniqueStat.MOVE_SPEED) > 0) {
            lines.set(9, color(equipped, book, TechniqueStat.MOVE_SPEED) + "移动速度增加：" + book.value(TechniqueStat.MOVE_SPEED));
        }
    }

    /** 来源阅读页把空档位与最上档都排为五；此规则独立于 Tooltip 的空槽颜色。 */
    private static String color(TechniqueBook equipped, TechniqueBook book, TechniqueStat stat) {
        int current = rank(equipped == null ? "" : equipped.definition().tier(stat));
        int incoming = rank(book.definition().tier(stat));
        return current > incoming ? "§4" : current < incoming ? "§a" : "§f";
    }

    private static int rank(String tier) {
        return switch (tier) {
            case "最下档" -> 1;
            case "下档" -> 2;
            case "中档" -> 3;
            case "上档" -> 4;
            default -> 5;
        };
    }

    private static void skillLines(List<String> lines, Player player, SkillBook book, DecimalFormat whole) {
        boolean owned = book.ownedBy(player.getUUID());
        lines.set(2, owned ? book.intro() : body(book.body()));
        lines.set(3, owned ? "" : level(book.levelText()));
        if (book.kind() != SkillKind.SPELL) return;
        if (owned) {
            lines.set(3, book.manaAttack() > 0 ? "基础法术伤害：" + book.manaAttack() + "（伤害倍率：" + book.damageMultiplier() + "）"
                    : "功法基础精神伤害：" + book.number("jinengsoulatt") + "（伤害倍率：" + book.damageMultiplier() + "）");
        }
        lines.set(4, book.manaAttack() > 0 ? "技能灵力消耗倍率：" + book.consumption() + "（此值乘以法术攻击力得到的值，才是最终消耗的灵力值）"
                : "技能精力消耗倍率：" + book.consumption() + "（此值乘以精神攻击力得到的值，才是最终消耗的精力值）");
        StringBuilder cooldown = new StringBuilder("技能冷却：");
        String ignore = "";
        SkillElement[] elements = SkillElement.values();
        for (int index = 0; index < elements.length; index++) {
            double change = book.cooldown(elements[index]);
            if (change > 0) cooldown.append(" §4").append(ELEMENT_NAMES[index]).append('+').append(whole.format(change)).append("秒");
            else if (change < 0) cooldown.append(" §a").append(ELEMENT_NAMES[index]).append('-').append(whole.format(-change)).append("秒");
            double allowed = book.cooldownIgnore(elements[index]);
            if (allowed > 0) {
                ignore = ELEMENT_NAMES[index] + (elements[index] == SkillElement.YINYANG ? "道法" : "属性") + "冷却时间在"
                        + whole.format(allowed) + "秒内时，可以正常释放技能。";
            }
        }
        lines.set(5, cooldown.toString());
        lines.set(6, ignore);
    }
}
