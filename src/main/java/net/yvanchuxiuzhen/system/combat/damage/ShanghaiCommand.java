package net.yvanchuxiuzhen.system.combat.damage;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;

/**
 * 命令 {@code /shanghai <类别> <攻击者UUID或无> <是否固定> <基础量> <倍率>}（来源 {@code ShanghaiCommand}，没有权限要求）：
 * 对命令执行者（{@code execute as} 指定的实体）造成一次灵力或神识伤害，见 {@link Shanghai}。
 * 参数按空白分隔，缺省为空串，数字解析失败为 0；“无”、{@code wu}、{@code no}、{@code false} 表示没有攻击者。
 */
public final class ShanghaiCommand {
    private ShanghaiCommand() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ShanghaiCommand::onRegister);
    }

    private static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shanghai")
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(ShanghaiCommand::run))
                .executes(ShanghaiCommand::run));
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        Entity entity = context.getSource().getEntity();
        if (!(entity instanceof LivingEntity target)) {
            return 0;
        }
        String[] words = context.getInput().trim().split("\\s+");
        String attackerWord = word(words, 2);
        UUID attacker = null;
        if (!(attackerWord.equals("无") || attackerWord.equals("wu") || attackerWord.equals("no") || attackerWord.equals("false"))) {
            try {
                attacker = UUID.fromString(attackerWord);
            } catch (IllegalArgumentException exception) {
                // 来源按这个字符串查不到实体，随后对空来源取数据而抛出异常，命令失败，没有伤害。
                return 0;
            }
        }
        Shanghai.deal(context.getSource().getLevel(), target, new Shanghai.Request(
                DamageCategory.byLabel(word(words, 1)), attacker, word(words, 3).equals("true"), number(word(words, 4)), number(word(words, 5))));
        return 0;
    }

    private static String word(String[] words, int index) {
        return index < words.length ? words[index] : "";
    }

    private static double number(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
