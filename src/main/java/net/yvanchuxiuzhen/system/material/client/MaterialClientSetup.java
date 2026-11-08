package net.yvanchuxiuzhen.system.material.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.material.content.MaterialMenus;

/** 材料系统的原生千材录界面装配。 */
public final class MaterialClientSetup {
    private MaterialClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(MaterialMenus.ANALYSIS.get(), MaterialAnalysisScreen::new));
    }
}
