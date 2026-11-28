package net.yvanchuxiuzhen.system.liandan.api;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** 喂药前询问其他系统目标实体是否已被独占为交互式 NPC，避免炼丹反向依赖修士系统。 */
public final class MedicineTargetCheckEvent extends Event implements ICancellableEvent {
    private final Entity target;

    public MedicineTargetCheckEvent(Entity target) {
        this.target = target;
    }

    public Entity target() {
        return target;
    }
}
