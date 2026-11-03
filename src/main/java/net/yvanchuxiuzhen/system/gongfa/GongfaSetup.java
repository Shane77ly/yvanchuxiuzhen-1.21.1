package net.yvanchuxiuzhen.system.gongfa;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationExperienceEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSecondEvent;

/**
 * 功法系统的启动装配，只在 Mod 构造时调用一次：内容表、功法组件与功法栏附件，以及对修炼推进的监听
 * （每秒推进时 {@code xiuliankaiguan} 为“功法”则进行功法修炼，闭关修炼吸纳元气后给主修功法加经验）。
 */
public final class GongfaSetup {
    private static final ContentTables CONTENT = new ContentTables("gongfa");

    private GongfaSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        GongfaData.register(modBus);
        NeoForge.EVENT_BUS.addListener((CultivationSecondEvent event) -> onSecond(event.player()));
        NeoForge.EVENT_BUS.addListener((CultivationExperienceEvent event) -> TechniqueRules.onCultivationExperience(event.player(), event.experience()));
    }

    private static void onSecond(ServerPlayer player) {
        if (Cultivation.tizhiString(player, "xiuliankaiguan").equals("功法")) {
            TechniqueRules.train(player);
        }
    }
}
