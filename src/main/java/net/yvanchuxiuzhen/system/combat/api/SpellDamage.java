package net.yvanchuxiuzhen.system.combat.api;

import java.text.DecimalFormat;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yvanchuxiuzhen.system.combat.damage.Shanghai;
import net.yvanchuxiuzhen.system.combat.damage.ShanghaiCommand;

/**
 * 其他系统造成灵力与神识伤害的入口，对应来源经 {@code /shanghai} 命令发起的调用，结算见 {@link Shanghai}。
 * {@code attacker} 为空表示无来源；{@code fixed} 为真时攻击量就是 {@code base}，不叠加攻击方的属性。
 */
public final class SpellDamage {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");

    private SpellDamage() {
    }

    public static void deal(ServerLevel level, LivingEntity target, DamageCategory category, Entity attacker, boolean fixed, double base, double multiplier) {
        deal(level, target, category, attacker == null ? null : attacker.getUUID(), fixed, base, multiplier);
    }

    /** 攻击者以 UUID 给出（实体里记下的 {@code AttackUUID}）；查不到该实体时没有伤害。 */
    public static void deal(ServerLevel level, LivingEntity target, DamageCategory category, UUID attacker, boolean fixed, double base, double multiplier) {
        Shanghai.deal(level, target, new Shanghai.Request(category, attacker, fixed, base, multiplier));
    }

    /**
     * 按 {@code /shanghai} 命令行文字（含命令名在内的完整输入，如 {@code "shanghai 火 <UUID> false 3 1"}）结算，
     * 解析与命令完全相同。来源用字符串拼命令时，某一项为空会让后面的参数前移，调用处原样拼出同样的字符串即可复现。
     */
    public static void command(ServerLevel level, LivingEntity target, String input) {
        ShanghaiCommand.execute(level, target, input);
    }

    /**
     * 来源把攻击量与倍率经 {@code DecimalFormat("0")} 写成整数再传给命令的调用（四舍六入五成双）：
     * 效果与 {@link #deal} 相同，但 {@code base} 与 {@code multiplier} 先取整。
     */
    public static void dealRounded(ServerLevel level, LivingEntity target, DamageCategory category, UUID attacker, boolean fixed, double base, double multiplier) {
        deal(level, target, category, attacker, fixed, Double.parseDouble(WHOLE.format(base)), Double.parseDouble(WHOLE.format(multiplier)));
    }
}
