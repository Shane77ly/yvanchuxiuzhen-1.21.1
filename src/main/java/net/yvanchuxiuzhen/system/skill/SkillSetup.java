package net.yvanchuxiuzhen.system.skill;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 技能系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class SkillSetup {
    private static final ContentTables CONTENT = new ContentTables("skill");

    private SkillSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
