package net.yvanchuxiuzhen.system.gongfa;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.gongfa.content.ComprehensionTableBlock;
import net.yvanchuxiuzhen.system.gongfa.content.GongfaBlockEntities;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;
import net.yvanchuxiuzhen.system.gongfa.network.GongfaButtonPayload;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationExperienceEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSecondEvent;

/**
 * 功法系统的启动装配，只在 Mod 构造时调用一次：内容表（参悟台换用专属方块类型）、功法组件与功法栏附件、参悟台的方块实体、
 * 菜单与按钮，以及对修炼推进的监听
 * （每秒推进时 {@code xiuliankaiguan} 为“功法”则进行功法修炼，闭关修炼吸纳元气后给主修功法加经验）。
 */
public final class GongfaSetup {
    private static final ContentTables CONTENT = new ContentTables("gongfa")
            .blockFactory("gongfa:canwutai_block", traits -> ContentBlock.create(traits, ComprehensionTableBlock::new));

    private GongfaSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        GongfaData.register(modBus);
        GongfaBlockEntities.register(modBus);
        modBus.addListener(GongfaSetup::registerPayloads);
        NeoForge.EVENT_BUS.addListener((CultivationSecondEvent event) -> onSecond(event.player()));
        NeoForge.EVENT_BUS.addListener((CultivationExperienceEvent event) -> TechniqueRules.onCultivationExperience(event.player(), event.experience()));
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(GongfaButtonPayload.TYPE, GongfaButtonPayload.STREAM_CODEC, GongfaButtonPayload::handle);
    }

    private static void onSecond(ServerPlayer player) {
        if (Cultivation.tizhiString(player, "xiuliankaiguan").equals("功法")) {
            TechniqueRules.train(player);
        }
    }
}
