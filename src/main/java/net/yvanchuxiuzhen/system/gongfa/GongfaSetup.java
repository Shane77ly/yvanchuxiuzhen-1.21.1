package net.yvanchuxiuzhen.system.gongfa;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 功法系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class GongfaSetup {
    private static final ContentTables CONTENT = new ContentTables("gongfa");

    private GongfaSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
