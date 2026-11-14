package net.yvanchuxiuzhen.system.economy.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.economy.api.BankMenus;

/** 银行客户端保留来源登录与存取界面，服务端菜单持有库存和账户。 */
public final class EconomyClientSetup {
    private EconomyClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(BankMenus.BANK.get(), BankScreen::new);
            event.register(BankMenus.LOGIN.get(), BankLoginScreen::new);
        });
    }
}
