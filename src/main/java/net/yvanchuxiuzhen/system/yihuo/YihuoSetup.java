package net.yvanchuxiuzhen.system.yihuo;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 异火系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class YihuoSetup {
    private static final ContentTables CONTENT = new ContentTables("yihuo");

    private YihuoSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
