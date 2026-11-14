package net.yvanchuxiuzhen.system.economy;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.economy.api.BankMenus;
import net.yvanchuxiuzhen.system.economy.content.BankCardItem;
import net.yvanchuxiuzhen.system.economy.data.BankData;

/**
 * 经济系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class EconomySetup {
    private static final ContentTables CONTENT = new ContentTables("economy")
            .itemFactory("economy:bank_card", (properties, tooltip) -> new BankCardItem(properties, tooltip, false))
            .itemFactory("economy:bank_card_advanced", (properties, tooltip) -> new BankCardItem(properties, tooltip, true));

    private EconomySetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        BankData.register(modBus);
        BankMenus.register(modBus);
        NeoForge.EVENT_BUS.addListener(BankCardInteraction::rightClick);
        NeoForge.EVENT_BUS.addListener(BankUpdates::tick);
        NeoForge.EVENT_BUS.addListener(BankUpdates::stopped);
    }
}
