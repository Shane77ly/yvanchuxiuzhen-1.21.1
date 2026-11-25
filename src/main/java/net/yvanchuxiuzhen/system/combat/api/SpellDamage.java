package net.yvanchuxiuzhen.system.combat.api;

import java.text.DecimalFormat;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yvanchuxiuzhen.system.combat.damage.Shanghai;
import net.yvanchuxiuzhen.system.combat.damage.ShanghaiCommand;
import net.yvanchuxiuzhen.system.combat.data.CombatData;

/**
 * 其他系统造成灵力与神识伤害的入口，对应来源经 {@code /shanghai} 命令发起的调用，结算见 {@link Shanghai}。
 * {@code attacker} 为空表示无来源；{@code fixed} 为真时攻击量就是 {@code base}，不叠加攻击方的属性。
 */
public final class SpellDamage {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");

    private SpellDamage() {
    }

    public static PendingSpellAttack pendingAttack(Entity target) {
        return target.getData(CombatData.PENDING_ATTACK);
    }

    /** 热油的接触回调只登记待消费的无来源火系灵力攻击，不主动发起结算。 */
    public static void markHotOilContact(Entity target) {
        target.setData(CombatData.PENDING_ATTACK, new PendingSpellAttack(false, 2, "无", "火", 1, false));
    }

    /** 阴二阵法只登记来源的待消费伤害字段；本操作不发起伤害结算。 */
    public static void markFormationSoulAttack(Entity target, int rank) {
        target.setData(CombatData.PENDING_ATTACK, new PendingSpellAttack(true, rank * 4, "wu", "阴阳", 0.5 + rank * 0.5, false));
    }

    /** 阳一和阳四仅登记整数攻击量，实际伤害消费路径保持来源边界。 */
    public static void markFormationManaAttack(Entity target, int amount, String attackerUuid, double multiplier) {
        target.setData(CombatData.PENDING_ATTACK, new PendingSpellAttack(false, amount, attackerUuid, "阴阳", multiplier, false));
    }

    /** 阳三登记浮点攻击量，攻击者字段按来源从目标自身读取后原值写回。 */
    public static void markFormationFallingAttack(Entity target, double rank) {
        String existingAttacker = target.getData(CombatData.PENDING_ATTACK).attackerUuid();
        target.setData(CombatData.PENDING_ATTACK, new PendingSpellAttack(false, rank * 3, existingAttacker, "阴阳", rank * .5 + .5, false));
    }

    /** 剧情法术与精神攻击只登记来源待消费字段，不在脚本动作中提前结算。 */
    public static void markQuestAttack(Entity target, boolean soul, double amount, String attackerUuid, String damageType, double multiplier) {
        target.setData(CombatData.PENDING_ATTACK, new PendingSpellAttack(soul, amount, attackerUuid, damageType, multiplier, false));
    }

    /** 45 号火炮弹到期时只记录待消费火系独立攻击与施放者。 */
    public static void markCannonBlast(Entity target, double amount, String attackerUuid) {
        target.setData(CombatData.PENDING_ATTACK, new PendingSpellAttack(false, amount, attackerUuid, "火", 1, true));
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
