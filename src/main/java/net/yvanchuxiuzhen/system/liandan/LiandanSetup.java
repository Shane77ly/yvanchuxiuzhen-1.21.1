package net.yvanchuxiuzhen.system.liandan;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 炼丹系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class LiandanSetup {
    private static final ContentTables CONTENT = new ContentTables("liandan");

    private LiandanSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
