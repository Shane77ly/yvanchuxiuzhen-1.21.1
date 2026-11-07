package net.yvanchuxiuzhen.system.yihuo;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/**
 * 异火系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class YihuoSetup {
    private static final ContentTables CONTENT = new ContentTables("yihuo");

    private YihuoSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.blockFactory("yihuo:fire", traits -> ContentBlock.create(traits, FireBlock::new));
        YihuoEffects.register(CONTENT);
        CONTENT.register(modBus);
        YihuoBlockEntities.register(modBus);
    }
}
