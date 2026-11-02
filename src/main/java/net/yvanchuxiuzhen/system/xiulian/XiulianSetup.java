package net.yvanchuxiuzhen.system.xiulian;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 修炼系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class XiulianSetup {
    private static final ContentTables CONTENT = new ContentTables("xiulian");

    private XiulianSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
