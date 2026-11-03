package net.yvanchuxiuzhen.system.npc;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 修士系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class NpcSetup {
    private static final ContentTables CONTENT = new ContentTables("npc");

    private NpcSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
