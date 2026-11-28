package net.yvanchuxiuzhen.system.combat.damage;

import com.mojang.util.UndashedUuid;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;

/** 手动伤害命令与存档攻击者字段共用的无来源标志及 UUID 解析。 */
public final class DamageFields {
    private static final Pattern WORD = Pattern.compile("\\S+");

    private DamageFields() { }

    public static void dealSaved(ServerLevel level, LivingEntity target, DamageCategory category, String owner,
            boolean fixed, double base, double multiplier) {
        // 正常 UUID 和单词标志无须分词；只在保存的文本含空白时读取多个词。
        if (!owner.isEmpty() && owner.chars().noneMatch(c -> c == ' ' || c >= '\t' && c <= '\r')) {
            Shanghai.Request damage = request(category, owner, fixed, base, multiplier);
            if (damage != null) Shanghai.deal(level, target, damage);
            return;
        }
        Matcher tokens = WORD.matcher(owner);
        Shanghai.Request damage;
        if (!tokens.find()) {
            // 空主人使固定标志前移为攻击者：true 无效；false 表示无来源，原倍率前移为攻击量。
            if (fixed) return;
            damage = new Shanghai.Request(category, null, false, multiplier, 0);
        } else {
            String first = tokens.group();
            if (tokens.find()) {
                // 多词主人占据后续位置；缺少数字时该位置是原固定标志，无法解析为数值。
                fixed = tokens.group().equals("true");
                if (!tokens.find()) return;
                base = number(tokens.group());
                multiplier = tokens.find() ? number(tokens.group()) : 0;
            }
            damage = request(category, first, fixed, base, multiplier);
        }
        if (damage != null) Shanghai.deal(level, target, damage);
    }

    static Shanghai.Request request(DamageCategory category, String attackerWord, boolean fixed, double base, double multiplier) {
        UUID attacker = null;
        if (!(attackerWord.equals("无") || attackerWord.equals("wu") || attackerWord.equals("no") || attackerWord.equals("false"))) {
            try {
                attacker = UndashedUuid.fromStringLenient(attackerWord);
            } catch (IllegalArgumentException exception) {
                // 来源按这个字符串查不到实体，随后对空来源取数据而抛出异常，命令失败，没有伤害。
                return null;
            }
        }
        return new Shanghai.Request(category, attacker, fixed, base, multiplier);
    }

    static double number(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
