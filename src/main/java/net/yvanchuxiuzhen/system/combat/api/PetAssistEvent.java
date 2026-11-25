package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.neoforged.bus.api.Event;

/**
 * 玩家攻击目标或受击时通知周围已驯服宠物协助锁定 {@code target}，在 {@code NeoForge.EVENT_BUS} 上发出。
 * 由持有宠物 AI 状态的系统订阅并写入其临时攻击目标。
 */
public final class PetAssistEvent extends Event {
    private final TamableAnimal pet;
    private final Entity target;

    public PetAssistEvent(TamableAnimal pet, Entity target) {
        this.pet = pet;
        this.target = target;
    }

    public TamableAnimal pet() {
        return pet;
    }

    public Entity target() {
        return target;
    }
}
