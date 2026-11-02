package net.yvanchuxiuzhen.system.guchong;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 蛊虫系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class GuchongSetup {
    private static final ContentTables CONTENT = new ContentTables("guchong");

    private GuchongSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
