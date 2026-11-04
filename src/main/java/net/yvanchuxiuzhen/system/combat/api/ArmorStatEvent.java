package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 伤害结算向装备系统查询四件护甲炼器数值（来源读每件护甲物品 NBT {@code lianqizong} 下的 {@code attack_Def}、
 * {@code soul_Def}、{@code mana_Att}，数值的存放归 lianqi 与 equipment）。监听者用 {@link #add} 累加四件护甲的该项之和。
 * 在 {@code NeoForge.EVENT_BUS} 上发出。
 */
public final class ArmorStatEvent extends Event {
    private final LivingEntity entity;
    private final String key;
    private double sum;

    public ArmorStatEvent(LivingEntity entity, String key) {
        this.entity = entity;
        this.key = key;
    }

    public LivingEntity entity() {
        return entity;
    }

    /** 来源的键名：{@code attack_Def}、{@code soul_Def} 或 {@code mana_Att}。 */
    public String key() {
        return key;
    }

    public double sum() {
        return sum;
    }

    public void add(double value) {
        sum += value;
    }
}
