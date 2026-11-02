package net.yvanchuxiuzhen.system.relation;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 关系系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class RelationSetup {
    private static final ContentTables CONTENT = new ContentTables("relation");

    private RelationSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
