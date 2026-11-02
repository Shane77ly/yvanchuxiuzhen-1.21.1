package net.yvanchuxiuzhen.system.zhenfa;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 阵法系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class ZhenfaSetup {
    private static final ContentTables CONTENT = new ContentTables("zhenfa");

    private ZhenfaSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
