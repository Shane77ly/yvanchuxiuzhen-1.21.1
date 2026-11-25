package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 破甲附魔命中仍存活的目标时在 {@code NeoForge.EVENT_BUS} 上发出，通知持有尸体采集计数状态的系统将未记采的计数置为 1。
 */
public final class ArmorPierceMarkEvent extends Event {
    private final LivingEntity victim;

    public ArmorPierceMarkEvent(LivingEntity victim) {
        this.victim = victim;
    }

    public LivingEntity victim() {
        return victim;
    }
}
