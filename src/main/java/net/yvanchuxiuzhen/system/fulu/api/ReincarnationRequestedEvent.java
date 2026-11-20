package net.yvanchuxiuzhen.system.fulu.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/** 轮回符确认完成或生物食用浮生丹后，请求跨所有者执行轮回。 */
public final class ReincarnationRequestedEvent extends Event {
    private final LivingEntity entity;
    private final ServerLevel sourceLevel;

    public ReincarnationRequestedEvent(LivingEntity entity, ServerLevel sourceLevel) {
        this.entity = entity;
        this.sourceLevel = sourceLevel;
    }

    public LivingEntity entity() {
        return entity;
    }

    public ServerLevel sourceLevel() {
        return sourceLevel;
    }
}
