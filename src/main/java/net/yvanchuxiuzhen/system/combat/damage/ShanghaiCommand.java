package net.yvanchuxiuzhen.system.combat.damage;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;

/**
 * 命令 {@code /shanghai <类别> <攻击者UUID或无> <是否固定> <基础量> <倍率>}（来源 {@code ShanghaiCommand}，没有权限要求）：
 * 对命令执行者（{@code execute as} 指定的实体）造成一次灵力或神识伤害，见 {@link Shanghai}。
 * 参数按空白分隔，缺省为空串，数字解析失败为 0；“无”、{@code wu}、{@code no}、{@code false} 表示没有攻击者。
 * 攻击者 UUID 接受标准连字符或无连字符形式；解析无效或本维度未加载的来源不造成伤害。
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
        execute(context.getSource().getLevel(), target, context.getInput());
        return 0;
    }

    /** 手动命令按空白取词，缺省数字与非法数字均按零结算。 */
    private static void execute(ServerLevel level, LivingEntity target, String input) {
        String[] words = input.trim().split("\\s+");
        Shanghai.Request request = DamageFields.request(DamageCategory.byLabel(word(words, 1)), word(words, 2), word(words, 3).equals("true"), DamageFields.number(word(words, 4)), DamageFields.number(word(words, 5)));
        if (request != null) Shanghai.deal(level, target, request);
    }

    private static String word(String[] words, int index) {
        return index < words.length ? words[index] : "";
    }

}
