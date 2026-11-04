package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 攻击者命中不友好的目标时发出（来源 {@code EntityHurt} 的命中处理，在 {@code NeoForge.EVENT_BUS} 上），
 * 所属系统据此执行自己的命中规则：gongfa 的功法特效（流星春水、星君诀、金廷落、血魔祭魂秘法），
 * creature 对妖兽的打断（{@code heavy} 为真：伤害量不小于目标最大生命的 1/16，来源让带技能的妖兽结束技能并标记被打断）。
 */
public final class HitEvent extends Event {
    private final LivingEntity victim;
    private final Entity attacker;
    private final double amount;
    private final boolean heavy;

    public HitEvent(LivingEntity victim, Entity attacker, double amount, boolean heavy) {
        this.victim = victim;
        this.attacker = attacker;
        this.amount = amount;
        this.heavy = heavy;
    }

    public LivingEntity victim() {
        return victim;
    }

    public Entity attacker() {
        return attacker;
    }

    public double amount() {
        return amount;
    }

    public boolean heavy() {
        return heavy;
    }
}
