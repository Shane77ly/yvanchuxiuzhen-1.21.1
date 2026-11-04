package net.yvanchuxiuzhen.system.combat.api;

import java.text.DecimalFormat;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yvanchuxiuzhen.system.combat.damage.Shanghai;

/**
 * 其他系统造成灵力与神识伤害的入口，对应来源经 {@code /shanghai} 命令发起的调用，结算见 {@link Shanghai}。
 * {@code attacker} 为空表示无来源；{@code fixed} 为真时攻击量就是 {@code base}，不叠加攻击方的属性。
 */
public final class SpellDamage {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");

    private SpellDamage() {
    }

    public static void deal(ServerLevel level, LivingEntity target, DamageCategory category, Entity attacker, boolean fixed, double base, double multiplier) {
        UUID source = attacker == null ? null : attacker.getUUID();
        Shanghai.deal(level, target, new Shanghai.Request(category, source, fixed, base, multiplier));
    }

    /**
     * 来源把攻击量与倍率经 {@code DecimalFormat("0")} 写成整数再传给命令的调用（四舍六入五成双）：
     * 效果与 {@link #deal} 相同，但 {@code base} 与 {@code multiplier} 先取整。
     */
    public static void dealRounded(ServerLevel level, LivingEntity target, DamageCategory category, Entity attacker, boolean fixed, double base, double multiplier) {
        deal(level, target, category, attacker, fixed, Double.parseDouble(WHOLE.format(base)), Double.parseDouble(WHOLE.format(multiplier)));
    }
}
