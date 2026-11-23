package net.yvanchuxiuzhen.system.material.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.material.content.MaterialMenus;

/** 材料系统的原生千材录界面与材料悬停提示装配。 */
public final class MaterialClientSetup {
    private MaterialClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(MaterialMenus.ANALYSIS.get(), MaterialAnalysisScreen::new));
        NeoForge.EVENT_BUS.addListener(MaterialTooltip::onTooltip);
    }
}
