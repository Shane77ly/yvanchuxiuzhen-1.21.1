package net.yvanchuxiuzhen.system.lianqi;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 炼器系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class LianqiSetup {
    private static final ContentTables CONTENT = new ContentTables("lianqi");

    private LianqiSetup() {
    }

    public static void register(IEventBus modBus) {
        LianqiRules.register();
        CONTENT.register(modBus);
    }
}
