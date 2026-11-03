package net.yvanchuxiuzhen.system.gongfa.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

/** 功法系统的客户端装配：功法书的提示文本。 */
public final class GongfaClientSetup {
    private GongfaClientSetup() {
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(TechniqueTooltip::onTooltip);
    }
}
