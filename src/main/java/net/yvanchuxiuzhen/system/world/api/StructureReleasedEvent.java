package net.yvanchuxiuzhen.system.world.api;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** 固定结构完成来源规定的等待后发布补充事实，各系统只更新自身拥有的数据。 */
public final class StructureReleasedEvent extends Event {
    private final ServerLevel level;
    private final String structureName;

    public StructureReleasedEvent(ServerLevel level, String structureName) {
        this.level = level;
        this.structureName = structureName;
    }

    public ServerLevel level() {
        return level;
    }

    public String structureName() {
        return structureName;
    }
}
